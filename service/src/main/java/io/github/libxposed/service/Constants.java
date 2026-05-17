package io.github.libxposed.service;

public final class Constants {
    static String SEND_BINDER_METHOD = "SendBinder";
    public static String SERVICE_AIDL_DESCRIPTOR = "io.github.libxposed.service.IXposedService";
    public static String SCOPE_CALLBACK_AIDL_DESCRIPTOR = "io.github.libxposed.service.IXposedScopeCallback";

    private Constants() {
        /* This class should not be instantiated */
    }
}
