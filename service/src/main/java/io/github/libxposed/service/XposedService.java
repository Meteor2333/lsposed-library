package io.github.libxposed.service;

import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.libxposed.annotation.SinceApi;
import io.github.libxposed.service.callback.HotReloadCallback;
import io.github.libxposed.service.callback.ScopeEventCallback;
import io.github.libxposed.service.exception.ServiceException;

public final class XposedService {
    private static final String TAG = "XposedService";

    private static IBinder mBinder = null;
    private static final Map<String, SharedPreferences> mPrefs = new HashMap<>();

    static void registerBinder(IBinder binder) {
        try {
            mBinder = binder;
            binder.linkToDeath(() -> mBinder = null, 0);
        } catch (Throwable t) {
            Log.e(TAG, "registerBinder", t);
        }
    }

    /**
     * Check if the Xposed service is available.
     *
     * @return True if the service is alive and can be called, false otherwise
     */
    public static boolean isAvailable() {
        return mBinder != null;
    }

    /**
     * Get the Xposed API version of current implementation.
     *
     * @return API version
     * @throws ServiceException If the service is dead or error occurred
     */
    public static int getApiVersion() {
        return callService(1, XposedService::nothing, Parcel::readInt, 0);
    }

    /**
     * Get the Xposed framework name of current implementation.
     *
     * @return Framework name
     * @throws ServiceException If the service is dead or error occurred
     */
    @NonNull
    public static String getFrameworkName() {
        return callService(2, XposedService::nothing, Parcel::readString, 0);
    }

    /**
     * Get the Xposed framework version of current implementation.
     *
     * @return Framework version
     * @throws ServiceException If the service is dead or error occurred
     */
    @NonNull
    public static String getFrameworkVersion() {
        return callService(3, XposedService::nothing, Parcel::readString, 0);
    }

    /**
     * Get the Xposed framework version code of current implementation.
     *
     * @return Framework version code
     * @throws ServiceException If the service is dead or error occurred
     */
    public static long getFrameworkVersionCode() {
        return callService(4, XposedService::nothing, Parcel::readLong, 0);
    }

    /**
     * Get the application scopes of current module.
     *
     * @return Module scopes
     * @throws ServiceException If the service is dead or error occurred
     */
    @NonNull
    public static List<String> getScopes() {
        return callService(10, XposedService::nothing, Parcel::createStringArrayList, 0);
    }

    /**
     * Request to add a new app to the module scope.
     *
     * @param packages Packages to be added
     * @param callback Callback to be invoked when the request is completed or error occurred
     * @throws ServiceException If the service is dead or an error occurred
     */
    public static void requestScope(@NonNull String packages, @NonNull ScopeEventCallback callback) {
        Consumer<Parcel> writer;
        if (getApiVersion() > 100) {
            writer = data -> {
                data.writeStringList(Collections.singletonList(packages));
                data.writeStrongInterface(callback);
            };
        } else {
            writer = data -> {
                data.writeString(packages);
                data.writeStrongInterface(callback);
            };
        }

        callService(11, writer, Function.identity(), IBinder.FLAG_ONEWAY);
    }

    /**
     * Remove an app from the module scope.
     *
     * @param packages Packages to be removed
     * @throws ServiceException If the service is dead or an error occurred
     */
    public static void removeScope(@NonNull String packages) {
        Consumer<Parcel> writer;
        if (getApiVersion() > 100) {
            writer = data -> data.writeStringList(Collections.singletonList(packages));
        } else {
            writer = data -> data.writeString(packages);
        }

        callService(12, writer, Function.identity(), 0);
    }

    /**
     * Get a list of currently running processes that are hooked by the module.
     * <p>
     * Returned targets can be passed to
     * {@link #hotReloadModule(HookedTarget, Bundle, HotReloadCallback)}. The pid, uid, process
     * name, and loaded version code are diagnostic values only.
     * </p>
     *
     * @return The list of hooked processes
     * @throws UnsupportedOperationException If the framework does not support service API 102
     * @throws ServiceException              If the service is dead or an error occurred
     */
    @NonNull
    @SinceApi(102)
    public List<HookedTarget> getRunningTargets() {
        List<HookedProcess> processes = callService(
                13,
                XposedService::nothing,
                reply -> reply.createTypedArrayList(HookedProcess.CREATOR),
                0
        );

        if (processes == null) {
            throw new ServiceException("Framework returns null");
        }

        List<HookedTarget> targets = new ArrayList<>(processes.size());
        for (HookedProcess process : processes) {
            if (process == null) {
                throw new ServiceException("Framework returns null target");
            }

            if (process.processName == null) {
                throw new ServiceException("Framework returns target with null processName");
            }

            int stateCode = process.state;
            if (stateCode < 0 || stateCode >= HotReloadCallback.Status.values().length) {
                throw new ServiceException("Invalid hooked target state: " + stateCode);
            }

            HookedTarget target = new HookedTarget(
                    process.targetId,
                    process.uid,
                    process.pid,
                    process.processName,
                    HookedTarget.State.values()[stateCode],
                    process.loadedVersionCode
            );
            targets.add(target);
        }

        return Collections.unmodifiableList(targets);
    }

    /**
     * Request hot reload for the module in the specified process. The process must be one of the
     * targets returned by {@link #getRunningTargets()}.
     * <p>
     * This method only validates and submits the request. The actual reload result is delivered
     * asynchronously through {@code callback}. If the framework cannot provide a valid new module
     * generation for the target, the callback receives {@link HotReloadCallback.Status#UNSUPPORTED}.
     * </p>
     * <p>
     * If the old module rejects reload by returning {@code false} from {@code onHotReloading},
     * the callback receives {@link HotReloadCallback.Status#FAILED} with a null message. If reload
     * fails because of an exception, the callback receives {@link HotReloadCallback.Status#FAILED}
     * with a framework-provided diagnostic message.
     * </p>
     * <p>
     * Hot reload is intended for loading a new module generation after the module app is updated.
     * It should not be used to propagate configuration changes. For configuration updates, use
     * {@link #getRemotePreferences(String)} and
     * {@link SharedPreferences.OnSharedPreferenceChangeListener}.
     * </p>
     * <p>
     * The optional data should contain only classloader-neutral values that can be unmarshalled
     * without the module's class loader. Do not put module-defined
     * {@link android.os.Parcelable} or {@link java.io.Serializable} objects in this bundle.
     * </p>
     *
     * @param target   The target process
     * @param data     Optional data to be passed to the old module
     * @param callback Callback to be invoked when the request completes or fails
     * @throws ServiceException  If the service is dead or an error occurred
     * @throws SecurityException If the target is invalid or no longer belongs to this module
     */
    @SinceApi(102)
    public void hotReloadModule(@NonNull HookedTarget target, @Nullable Bundle data, @NonNull HotReloadCallback callback) {
        callService(
                14,
                datax -> {
                    datax.writeLong(target.mTargetId);
                    datax.writeTypedObject(data, 0);
                    datax.writeStrongInterface(callback);
                },
                reply -> reply.createTypedArrayList(HookedProcess.CREATOR),
                0
        );
    }

    /**
     * Get remote preferences from Xposed framework.
     *
     * @param group Group name
     * @return The preferences
     * @throws ServiceException If the service is dead or error occurred
     */
    @NonNull
    public static SharedPreferences getRemotePreferences(@NonNull String group) {
        return mPrefs.computeIfAbsent(group, k -> {
            Bundle bundle = callService(
                    20,
                    data -> data.writeString(group),
                    reply -> reply.readTypedObject(Bundle.CREATOR),
                    0
            );
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) bundle.getSerializable("map");
            return new RemotePreferences(group, map);
        });
    }

    /**
     * Update a group of remote preferences.
     *
     * @param group Group name
     * @param editor Editor to be applied
     * @throws ServiceException If the service is dead or error occurred
     */
    public static void updateRemotePreferences(@NonNull String group, @NonNull SharedPreferences.Editor editor) {
        if (!(editor instanceof RemotePreferences.Editor remoteEditor)) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("delete", new HashSet<>(remoteEditor.mDelete));
        bundle.putSerializable("put", new HashMap<>(remoteEditor.mPut));
        callService(
                21,
                data -> {
                    data.writeString(group);
                    data.writeTypedObject(bundle, 0);
                },
                Function.identity(),
                0
        );
    }

    /**
     * Delete a group of remote preferences.
     *
     * @param group Group name
     * @throws ServiceException If the service is dead or error occurred
     */
    public static void deleteRemotePreferences(@NonNull String group) {
        mPrefs.remove(group);
        callService(
                22,
                data -> data.writeString(group),
                Function.identity(),
                0
        );
    }

    /**
     * List all files in the module's shared data directory.
     *
     * @return The file list
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    @NonNull
    public static String[] getRemoteFiles() {
        return callService(30, XposedService::nothing, Parcel::createStringArray, 0);
    }

    /**
     * Open a file in the module's shared data directory. The file will be created if not exists.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return The file descriptor
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    @NonNull
    public static ParcelFileDescriptor openRemoteFile(@NonNull String name) {
        return callService(
                31,
                data -> data.writeString(name),
                reply -> reply.readTypedObject(ParcelFileDescriptor.CREATOR),
                0
        );
    }

    /**
     * Delete a file in the module's shared data directory.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return true if successful, false if the file does not exist
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    public static boolean deleteRemoteFile(@NonNull String name) {
        return callService(
                32,
                data -> data.writeString(name),
                reply -> reply.readInt() != 0,
                0
        );
    }

    private static <T> T callService(int code, Consumer<Parcel> writer, Function<Parcel, T> reader, int flags) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(Constants.SERVICE_AIDL_DESCRIPTOR);
            writer.accept(data);
            mBinder.transact(code + 1, data, reply, flags);
            reply.readException();
            return reader.apply(reply);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static <T> void nothing(T t) {
    }
    
    private XposedService() {
        /* This class should not be instantiated */
    }
}