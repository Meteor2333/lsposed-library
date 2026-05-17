package io.github.libxposed.service.exception;

import android.os.RemoteException;

public class ServiceException extends RuntimeException {
    public ServiceException(RemoteException e) {
        super("Xposed service error", e);
    }
}