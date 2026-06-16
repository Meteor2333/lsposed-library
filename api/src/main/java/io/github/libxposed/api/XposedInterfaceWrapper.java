package io.github.libxposed.api;

import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.FileInputStream;

import io.github.libxposed.annotation.SinceApi;

public class XposedInterfaceWrapper implements XposedInterface {
    @Override
    @SinceApi(101)
    public int getApiVersion() {
        throw new UnsupportedOperationException();
    }

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
    @Deprecated
    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedPreferences getRemotePreferences(String group) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String[] listRemoteFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public FileInputStream openFileInput(String name) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ParcelFileDescriptor openRemoteFile(@NonNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SinceApi(102)
    public void detach() {
        throw new UnsupportedOperationException();
    }
}