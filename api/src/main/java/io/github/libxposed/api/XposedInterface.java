package io.github.libxposed.api;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

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
}
