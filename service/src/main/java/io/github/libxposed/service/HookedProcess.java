package io.github.libxposed.service;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Information about a process currently hooked by this module.
 */
public class HookedProcess implements Parcelable {
    public static final Parcelable.Creator<HookedProcess> CREATOR = new Parcelable.Creator<>() {
        @Override
        public HookedProcess createFromParcel(Parcel source) {
            HookedProcess out = new HookedProcess();
            out.readFromParcel(source);
            return out;
        }

        @Override
        public HookedProcess[] newArray(int size) {
            return new HookedProcess[size];
        }
    };

    /**
     * Opaque identifier assigned by the framework. Module apps must only pass this value back to
     * the service and must not infer ordering, lifetime, or process identity from it.
     */
    private long mTargetId = 0;
    /**
     * The process uid, provided for display and diagnostics.
     */
    private int mUid = 0;
    /**
     * The process id, provided for display and diagnostics. It must not be used as target identity.
     */
    private int mPid = 0;
    /**
     * The Android process name, provided for display and diagnostics.
     */
    private String mProcessName;
    /**
     * One of TARGET_STATE_*.
     */
    private State mState;
    /**
     * Version code of the module package loaded in this process. This is only a diagnostic value;
     * the framework may use a stronger internal code identity to determine state.
     */
    private long mLoadedVersionCode = 0;

    /**
     * State of a hooked target.
     */
    public enum State {
        /**
         * The target is running the currently installed module code.
         */
        UP_TO_DATE,

        /**
         * The target is still running old module code and may be hot-reloaded.
         */
        STALE,

        /**
         * The target is currently being hot-reloaded.
         */
        RELOADING,

        /**
         * The target's last hot reload attempt failed because the old module refused reload or
         * reload raised an exception.
         */
        FAILED
    }

    /**
     * Gets the target id.
     */
    long getTargetId() {
        return mTargetId;
    }

    /**
     * Gets the process uid, provided for display and diagnostics.
     */
    public int getUid() {
        return mUid;
    }

    /**
     * Gets the process id, provided for display and diagnostics.
     * It must not be used as target identity.
     */
    public int getPid() {
        return mPid;
    }

    /**
     * Gets the Android process name, provided for display and diagnostics.
     */
    @NonNull
    public String getProcessName() {
        return mProcessName;
    }

    /**
     * Gets the target state.
     */
    @NonNull
    public State getState() {
        return mState;
    }

    /**
     * Gets the version code of the module package loaded in this process.
     * This is only a diagnostic value; the framework may use a stronger internal code identity to
     * determine state.
     */
    public long getLoadedVersionCode() {
        return mLoadedVersionCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        int start = parcel.dataPosition();
        parcel.writeInt(0);
        parcel.writeLong(this.mTargetId);
        parcel.writeInt(this.mUid);
        parcel.writeInt(this.mPid);
        parcel.writeString(this.mProcessName);
        parcel.writeInt(this.mState.ordinal());
        parcel.writeLong(this.mLoadedVersionCode);

        int end = parcel.dataPosition();
        parcel.setDataPosition(start);
        parcel.writeInt(end - start);
        parcel.setDataPosition(end);
    }

    public void readFromParcel(Parcel parcel) {
        int start = parcel.dataPosition();
        int size = parcel.readInt();
        try {
            if (size < 4) {
                throw new BadParcelableException("Parcelable too small");
            }

            if (checkPosition(parcel, start, size)) return;
            this.mTargetId = parcel.readLong();
            if (checkPosition(parcel, start, size)) return;
            this.mUid = parcel.readInt();
            if (checkPosition(parcel, start, size)) return;
            this.mPid = parcel.readInt();
            if (checkPosition(parcel, start, size)) return;
            this.mProcessName = parcel.readString();
            if (checkPosition(parcel, start, size)) return;
            this.mState = State.values()[parcel.readInt()];
            if (checkPosition(parcel, start, size)) return;
            this.mLoadedVersionCode = parcel.readLong();
        } catch (Throwable th) {
            if (start <= Integer.MAX_VALUE - size) {
                parcel.setDataPosition(start + size);
            }

            throw th;
        }
    }
    
    private boolean checkPosition(Parcel parcel, int start, int total) {
        if (parcel.dataPosition() - start >= total) {
            if (start > Integer.MAX_VALUE - total) {
                throw new BadParcelableException("Overflow in the size of parcelable");
            }

            parcel.setDataPosition(start + total);
            return true;
        }
        
        return false;
    }
}