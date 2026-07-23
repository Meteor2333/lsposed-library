package io.github.libxposed.api;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileInputStream;

public class XposedInterfaceWrapper implements XposedInterface {
    @Override
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
    public void log(@NonNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(@NonNull String message, @NonNull Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String msg, @Nullable Throwable tr) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ApplicationInfo getModuleApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SharedPreferences getSharedPreferences(@NonNull String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SharedPreferences getRemotePreferences(@NonNull String group) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String[] listRemoteFiles() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public FileInputStream openFileInput(String name) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ParcelFileDescriptor openRemoteFile(@NonNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void detach() {
        throw new UnsupportedOperationException();
    }
}