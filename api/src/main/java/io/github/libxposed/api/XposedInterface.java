package io.github.libxposed.api;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.github.libxposed.annotation.SinceApi;

/**
 * Xposed interface for modules to operate on application processes.
 */
public interface XposedInterface {
    /**
     * Gets the runtime Xposed API version. Framework implementations <b>must not</b> override this method.
     */
    @SinceApi(101)
    int getApiVersion();

    /**
     * Gets the Xposed framework name of current implementation.
     */
    @NonNull
    String getFrameworkName();

    /**
     * Gets the Xposed framework version of current implementation.
     */
    @NonNull
    String getFrameworkVersion();

    /**
     * Gets the Xposed framework version code of current implementation.
     */
    long getFrameworkVersionCode();

    /**
     * Gets application info.
     *
     * @deprecated This method is not recommended to use.
     * Although it exists, it was rarely implemented in official framework releases,
     * so its lifecycle was very short.
     * <p>Use {@link #getModuleApplicationInfo()} instead.
     *
     * @return the application info
     */
    @Deprecated
    ApplicationInfo getApplicationInfo();

    /**
     * Gets the application info of the module.
     */
    @NonNull
    ApplicationInfo getModuleApplicationInfo();

    /**
     * Get shared preferences.
     *
     * @deprecated This method is not recommended to use.
     * Although it exists, it was rarely implemented in official framework releases,
     * so its lifecycle was very short.
     * <p>Use {@link #getRemotePreferences(String)} instead.
     *
     * @param name the name
     * @param mode the mode
     * @return the shared preferences
     */
    @Deprecated
    SharedPreferences getSharedPreferences(String name, int mode);

    /**
     * Get remote preferences stored in Xposed framework. Note that those are read-only in hooked apps.
     *
     * @param group Group name
     * @return The preferences
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    SharedPreferences getRemotePreferences(String group);

    /**
     * File list string [ ].
     *
     * @deprecated This method is not recommended to use.
     * Although it exists, it was rarely implemented in official framework releases,
     * so its lifecycle was very short.
     * <p>Use {@link #listRemoteFiles()} instead.
     *
     * @return the string [ ]
     */
    @Deprecated
    String[] fileList();

    /**
     * List all files in the module's shared data directory.
     *
     * @return The file list
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    String[] listRemoteFiles();

    /**
     * Open file input file input stream.
     *
     * @deprecated This method is not recommended to use.
     * Although it exists, it was rarely implemented in official framework releases,
     * so its lifecycle was very short.
     * <p>Use {@link #openRemoteFile(String)} instead.
     *
     * @param name the name
     * @return the file input stream
     * @throws FileNotFoundException the file not found exception
     */
    @Deprecated
    FileInputStream openFileInput(String name) throws FileNotFoundException;

    /**
     * Open a file in the module's shared data directory. The file is opened in read-only mode.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return The file descriptor
     * @throws FileNotFoundException         If the file does not exist or the path is forbidden
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    ParcelFileDescriptor openRemoteFile(@NonNull String name) throws FileNotFoundException;

    /**
     * Stops all subsequent lifecycle callbacks for the <b>current module entry</b> in the current
     * process. After this method is called, the framework removes its reference to the entry
     * instance and will no longer invoke any lifecycle callbacks (such as
     * {@link XposedModuleInterface#onPackageLoaded},
     * {@link XposedModuleInterface#onHotReloading}, etc.) on the entry instance that
     * called this method. Only lifecycle callbacks are affected; all {@link XposedInterface} APIs
     * remain fully functional.
     *
     * <p>If the module declares multiple entry classes, only the entry that calls this method is
     * affected. Other entries continue to receive their lifecycle callbacks as normal.</p>
     *
     * <p>This method is idempotent. Calling it multiple times has the same effect as calling it once.</p>
     *
     * <p>If the module expects its classloader to become collectible after detaching, it must also
     * remove module-owned references and execution contexts that keep module objects reachable, such
     * as installed hooks, Java threads, and callbacks held by system or app objects. If native code
     * is still running after all Java references to the module classloader are cleared, later runtime
     * unloading of native libraries may crash the process; this is a module lifecycle bug.</p>
     *
     * <p>Typical use cases include:</p>
     * <ul>
     *     <li>The module entry has finished all its initialization work and no longer needs to
     *     respond to further package loading events.</li>
     *     <li>For modules that target multiple apps with a dedicated entry class per app: if the
     *     entry detects it is not loaded in its target app, it can call this method immediately to
     *     avoid receiving any further callbacks.</li>
     *     <li>Calling this method together with unhooking all registered hooks, so that the module
     *     classloader can be garbage collected when no longer needed.</li>
     * </ul>
     */
    @SinceApi(102)
    void detach();
}
