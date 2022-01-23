package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;

public class NanoAppBinary implements Parcelable {

    public NanoAppBinary(byte[] appBinary) {
        throw new RuntimeException("Stub!");
    }

    protected NanoAppBinary(Parcel in) {
    }

    public static final Creator<NanoAppBinary> CREATOR = new Creator<NanoAppBinary>() {
        @Override
        public NanoAppBinary createFromParcel(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public NanoAppBinary[] newArray(int size) {

            throw new RuntimeException("Stub!");
        }
    };

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }
}
