package io.github.libxposed.service.callback;

import android.os.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.github.libxposed.service.Constants;
import io.github.libxposed.service.XposedService;

public class ScopeEventCallback implements IInterface {
    private static final int TRANSACTION_PROMPTED100_OR_APPROVED101 = 1 + 1;
    private static final int TRANSACTION_APPROVED100_OR_FAILED101 = 2 + 1;
    private static final int TRANSACTION_DENIED100 = 3 + 1;
    private static final int TRANSACTION_TIMEOUT100 = 4 + 1;
    private static final int TRANSACTION_FAILED100 = 5 + 1;

    private final Binder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            if (code >= FIRST_CALL_TRANSACTION && code <= LAST_CALL_TRANSACTION) {
                data.enforceInterface(Constants.SCOPE_CALLBACK_AIDL_DESCRIPTOR);
            }

            switch (code) {
                case INTERFACE_TRANSACTION: {
                    if (reply != null) {
                        reply.writeString(Constants.SCOPE_CALLBACK_AIDL_DESCRIPTOR);
                    }
                    break;
                }
                case TRANSACTION_PROMPTED100_OR_APPROVED101: {
                    if (XposedService.getApiVersion() > 100) {
                        List<String> packages = data.createStringArrayList();
                        if (packages != null) {
                            for (String approved : packages) {
                                onApproved(approved);
                            }
                        }
                    }
                    break;
                }
                case TRANSACTION_APPROVED100_OR_FAILED101: {
                    if (XposedService.getApiVersion() > 100) {
                        onFailed(data.readString());
                    } else {
                        onApproved(data.readString());
                    }
                    break;
                }
                case TRANSACTION_DENIED100, TRANSACTION_TIMEOUT100: {
                    onFailed(data.readString());
                    break;
                }
                case TRANSACTION_FAILED100: {
                    data.readString();
                    onFailed(data.readString());
                    break;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }

            return true;
        }
    };

    @Override
    public IBinder asBinder() {
        return mBinder;
    }

    /**
     * Callback when the request is approved.
     *
     * @param packageName Package name of requested app
     */
    public void onApproved(String packageName) {

    }

    /**
     * Callback when the request is failed.
     *
     * @param message Error message
     */
    public void onFailed(String message) {

    }
}
