package io.github.libxposed.service.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.libxposed.annotation.SinceApi;
import io.github.libxposed.service.Constants;
import io.github.libxposed.service.exception.ServiceException;

/**
 * Callback interface for hot reload requests.
 */
@SinceApi(102)
public class HotReloadCallback implements IInterface {
    private static final int TRANSACTION_RESULT = 1 + 1;

    private final Binder mBinder = new Binder() {
        {
            attachInterface(HotReloadCallback.this, Constants.HOT_RELOAD_CALLBACK_AIDL_DESCRIPTOR);
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            if (code >= FIRST_CALL_TRANSACTION && code <= LAST_CALL_TRANSACTION) {
                data.enforceInterface(Constants.HOT_RELOAD_CALLBACK_AIDL_DESCRIPTOR);
            }

            switch (code) {
                case INTERFACE_TRANSACTION: {
                    if (reply != null) {
                        reply.writeString(Constants.HOT_RELOAD_CALLBACK_AIDL_DESCRIPTOR);
                    }
                    break;
                }
                case TRANSACTION_RESULT: {
                    int statusCode = data.readInt();
                    String message = data.readString();
                    if (statusCode < 0 || statusCode >= Status.values().length) {
                        throw new ServiceException("Invalid hot reload status code: " + statusCode);
                    }

                    onResult(Status.values()[statusCode], message);
                    break;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }

            return true;
        }
    };

    /**
     * Hot reload completion status.
     */
    public enum Status {
        /**
         * Hot reload completed successfully.
         */
        SUCCEEDED,

        /**
         * The old module refused reload, or hot reload raised an exception.
         * <p>
         * When the old module refuses reload by returning {@code false} from
         * {@code onHotReloading}, the message is null. When reload fails
         * because of an exception, the message contains a framework-provided diagnostic string.
         * </p>
         */
        FAILED,

        /**
         * The target does not support hot reload.
         * <p>
         * For example, this can be returned for modules that do not declare exactly one Java
         * entry class or targets for which the framework cannot provide a valid new module
         * generation.
         * </p>
         */
        UNSUPPORTED,

        /**
         * The target is already being hot-reloaded.
         */
        IN_PROGRESS,

        /**
         * The target process died before hot reload could complete.
         */
        PROCESS_DIED
    }

    @Override
    public IBinder asBinder() {
        return mBinder;
    }

    /**
     * Called when hot reload completes or fails.
     * <p>
     * This callback may run on a Binder thread. Dispatch to the main thread before touching UI.
     * </p>
     *
     * @param status  The completion status
     * @param message Optional framework-provided diagnostic message. For {@link Status#FAILED}, a
     *                null message means the old module refused reload by returning {@code false} from
     *                {@code onHotReloading}; a non-null message describes a reload exception.
     */
    public void onResult(@NonNull Status status, @Nullable String message) {
    }
}