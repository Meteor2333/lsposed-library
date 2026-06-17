package io.github.libxposed.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.libxposed.service.Constants;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.callback.ServiceLifecycleCallback;

public final class XposedProvider extends ContentProvider {
    private static final String TAG = "XposedProvider";

    private static final XposedService mService = new XposedService();
    private static final List<SLCWithLooper> mLifecycleListeners = new ArrayList<>();

    // A wrapper to ensure callbacks are posted on the correct thread
    private record SLCWithLooper(Looper looper, ServiceLifecycleCallback callback) implements ServiceLifecycleCallback {
        @Override
        public void onServiceBind(@NonNull XposedService service) {
            if (looper.getThread().isAlive()) {
                new Handler(looper).post(() -> callback.onServiceBind(service));
            }
        }

        @Override
        public void onServiceDied() {
            if (looper.getThread().isAlive()) {
                new Handler(looper).post(callback::onServiceDied);
            }
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (method.equals(Constants.SEND_BINDER_METHOD) && extras != null) {
            IBinder binder = extras.getBinder("binder");
            if (binder != null) {
                Log.d(TAG, "binder received: " + binder);
                try {
                    registerBinder(binder);
                    binder.linkToDeath(XposedProvider::unregisterBinder, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "registerBinder", e);
                }
            }

            return new Bundle();
        }

        return super.call(method, arg, extras);
    }

    public static XposedService getService() {
        return mService;
    }

    public static Optional<XposedService> getServiceIfAvailable() {
        return mService.isAvailable() ? Optional.of(mService) : Optional.empty();
    }

    public static void registerLifecycleListener(ServiceLifecycleCallback listener) {
        mLifecycleListeners.add(new SLCWithLooper(Looper.myLooper(), listener));
        if (mService.isAvailable()) {
            listener.onServiceBind(mService);
        }
    }

    public static void unregisterLifecycleListener(ServiceLifecycleCallback listener) {
        mLifecycleListeners.removeIf(wrapper -> wrapper.callback.equals(listener));
    }

    private static void registerBinder(IBinder binder) {
        mService.mBinder = binder;
        mLifecycleListeners.forEach(listener -> listener.onServiceBind(mService));
    }

    private static void unregisterBinder() {
        mService.mBinder = null;
        mLifecycleListeners.forEach(ServiceLifecycleCallback::onServiceDied);
    }
}