package io.github.libxposed.api;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class XposedInterfaceWrapper implements XposedInterface {
    @NonNull
    @Override
    public String getFrameworkName() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String getFrameworkVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getFrameworkVersionCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedPreferences getRemotePreferences(String group) {
        throw new UnsupportedOperationException();
    }
}