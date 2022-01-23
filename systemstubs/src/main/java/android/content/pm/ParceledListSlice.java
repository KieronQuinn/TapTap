/*
 * Copyright (C) 2011 The Android Open Source Project
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
package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

/**
 * Transfer a large list of Parcelable objects across an IPC.  Splits into
 * multiple transactions if needed.
 *
 * @see BaseParceledListSlice
 */
public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {

    public ParceledListSlice(List<T> list) {
        super(list);
    }
    private ParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected void writeElement(T parcelable, Parcel dest, int callFlags) {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected void writeParcelableCreator(T parcelable, Parcel dest) {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        throw new RuntimeException("Stub!");
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR =
            new Parcelable.ClassLoaderCreator<ParceledListSlice>() {

        public ParceledListSlice createFromParcel(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ParceledListSlice[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };
}