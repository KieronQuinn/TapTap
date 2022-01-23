/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.hardware.location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;

import java.util.Arrays;

/**
 * @hide
 */
public class ContextHubInfo implements Parcelable {


    /*
     * TODO(b/67734082): Deprecate this constructor and mark private fields as final.
     */
    public ContextHubInfo() {
    }

    /**
     * returns the maximum number of bytes that can be sent per message to the hub
     *
     * @return int - maximum bytes that can be transmitted in a
     *         single packet
     */
    public int getMaxPacketLengthBytes() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the context hub unique identifer
     *
     * @return int - unique system wide identifier
     */
    public int getId() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get a string as a hub name
     *
     * @return String - a name for the hub
     */
    public String getName() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get a string as the vendor name
     *
     * @return String - a name for the vendor
     */
    public String getVendor() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get tool chain string
     *
     * @return String - description of the tool chain
     */
    public String getToolchain() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get platform version
     *
     * @return int - platform version number
     */
    public int getPlatformVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get static platform version number
     *
     * @return int - platform version number
     */
    public int getStaticSwVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the tool chain version
     *
     * @return int - the tool chain version
     */
    public int getToolchainVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the peak processing mips the hub can support
     *
     * @return float - peak MIPS that this hub can deliver
     */
    public float getPeakMips() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the stopped power draw in milliwatts
     * This assumes that the hub enter a stopped state - which is
     * different from the sleep state. Latencies on exiting the
     * sleep state are typically higher and expect to be in multiple
     * milliseconds.
     *
     * @return float - power draw by the hub in stopped state
     */
    public float getStoppedPowerDrawMw() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the power draw of the hub in sleep mode. This assumes
     * that the hub supports a sleep mode in which the power draw is
     * lower than the power consumed when the hub is actively
     * processing. As a guideline, assume that the hub should be
     * able to enter sleep mode if it knows reliably on completion
     * of some task that the next interrupt/scheduled work item is
     * at least 250 milliseconds later.
     *
     * @return float - sleep power draw in milli watts
     */
    public float getSleepPowerDrawMw() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the peak powe draw of the hub. This is the power consumed
     * by the hub at maximum load.
     *
     * @return float - peak power draw
     */
    public float getPeakPowerDrawMw() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the sensors supported by this hub
     *
     * @return int[] - all the supported sensors on this hub
     *
     */
    public int[] getSupportedSensors() {
        throw new RuntimeException("Stub!");
    }

    /**
     * get the various memory regions on this hub
     *
     * @return MemoryRegion[] - all the memory regions on this hub
     *
     */
    public MemoryRegion[] getMemoryRegions() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @return the CHRE platform ID as defined in chre/version.h
     */
    public long getChrePlatformId() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @return the CHRE API's major version as defined in chre/version.h
     */
    public byte getChreApiMajorVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @return the CHRE API's minor version as defined in chre/version.h
     */
    public byte getChreApiMinorVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @return the CHRE patch version as defined in chre/version.h
     */
    public short getChrePatchVersion() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Dump the internal state as a ContextHubInfoProto to the given ProtoOutputStream.
     *
     * If the output belongs to a sub message, the caller is responsible for wrapping this function
     * between {@link ProtoOutputStream#start(long)} and {@link ProtoOutputStream#end(long)}.
     */
    public void dump(ProtoOutputStream proto) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean equals(@Nullable Object object) {
        throw new RuntimeException("Stub!");
    }

    private ContextHubInfo(Parcel in) {
        throw new RuntimeException("Stub!");
    }

    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    public void writeToParcel(Parcel out, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static final @androidx.annotation.NonNull Parcelable.Creator<ContextHubInfo> CREATOR
            = new Parcelable.Creator<ContextHubInfo>() {
        public ContextHubInfo createFromParcel(Parcel in) {
            return new ContextHubInfo(in);
        }

        public ContextHubInfo[] newArray(int size) {
            return new ContextHubInfo[size];
        }
    };
}