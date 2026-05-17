package io.github.libxposed.api;

import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Xposed interface for modules to operate on application processes.
 */
public interface XposedInterface {
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
}
