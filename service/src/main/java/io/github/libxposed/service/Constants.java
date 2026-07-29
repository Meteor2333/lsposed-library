package io.github.libxposed.service;

import io.github.libxposed.annotation.InternalApi;

@InternalApi
public final class Constants {
    public static final String SEND_BINDER_METHOD = "SendBinder";
    public static final String SERVICE_AIDL_DESCRIPTOR = "io.github.libxposed.service.IXposedService";
    public static final String SCOPE_CALLBACK_AIDL_DESCRIPTOR = "io.github.libxposed.service.IXposedScopeCallback";
    public static final String HOT_RELOAD_CALLBACK_AIDL_DESCRIPTOR = "io.github.libxposed.service.IHotReloadCallback";

    private Constants() {
        /* This class should not be instantiated */
    }
}
