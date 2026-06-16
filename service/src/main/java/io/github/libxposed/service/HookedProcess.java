package io.github.libxposed.service;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import io.github.libxposed.annotation.SinceApi;

/**
 * Information about a process currently hooked by this module.
 */
@SinceApi(102)
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
    public long targetId = 0;
    /**
     * The process uid, provided for display and diagnostics.
     */
    public int uid = 0;
    /**
     * The process id, provided for display and diagnostics. It must not be used as target identity.
     */
    public int pid = 0;
    /**
     * The Android process name, provided for display and diagnostics.
     */
    public String processName;
    /**
     * One of TARGET_STATE_*.
     */
    public int state = 0;
    /**
     * Version code of the module package loaded in this process. This is only a diagnostic value;
     * the framework may use a stronger internal code identity to determine state.
     */
    public long loadedVersionCode = 0;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        int start = parcel.dataPosition();
        parcel.writeInt(0);
        parcel.writeLong(this.targetId);
        parcel.writeInt(this.uid);
        parcel.writeInt(this.pid);
        parcel.writeString(this.processName);
        parcel.writeInt(this.state);
        parcel.writeLong(this.loadedVersionCode);

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
            this.targetId = parcel.readLong();
            if (checkPosition(parcel, start, size)) return;
            this.uid = parcel.readInt();
            if (checkPosition(parcel, start, size)) return;
            this.pid = parcel.readInt();
            if (checkPosition(parcel, start, size)) return;
            this.processName = parcel.readString();
            if (checkPosition(parcel, start, size)) return;
            this.state = parcel.readInt();
            if (checkPosition(parcel, start, size)) return;
            this.loadedVersionCode = parcel.readLong();
        } catch (Throwable th) {
            if (start <= Integer.MAX_VALUE - size) {
                parcel.setDataPosition(start + size);
            }

            throw th;
        }
    }
    
    public boolean checkPosition(Parcel parcel, int start, int total) {
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