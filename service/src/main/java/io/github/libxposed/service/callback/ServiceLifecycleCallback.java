package io.github.libxposed.service.callback;

import androidx.annotation.NonNull;

import io.github.libxposed.service.XposedService;

/**
 * Callback interface for Xposed service.
 */
public interface ServiceLifecycleCallback {
    /**
     * Callback when the service is connected.<br/>
     * This method could be called multiple times if multiple Xposed frameworks exist.
     *
     * @param service Service instance
     */
    void onServiceBind(@NonNull XposedService service);

    /**
     * Callback when the service is dead.
     */
    void onServiceDied();
}
