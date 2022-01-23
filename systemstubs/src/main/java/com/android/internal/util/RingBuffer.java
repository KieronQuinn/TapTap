
/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.internal.util;

/**
 * A simple ring buffer structure with bounded capacity backed by an array.
 * Events can always be added at the logical end of the buffer. If the buffer is
 * full, oldest events are dropped when new events are added.
 */
public class RingBuffer<T> {

    public RingBuffer(Class<T> c, int capacity) {
        throw new RuntimeException("Stub!");
    }

    public int size() {
        throw new RuntimeException("Stub!");
    }

    public boolean isEmpty() {
        throw new RuntimeException("Stub!");
    }

    public void clear() {
        throw new RuntimeException("Stub!");
    }

    public void append(T t) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns object of type <T> at the next writable slot, creating one if it is not already
     * available. In case of any errors while creating the object, <code>null</code> will
     * be returned.
     */
    public T getNextSlot() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @return a new object of type <T> or null if a new object could not be created.
     */
    protected T createNewItem() {
        throw new RuntimeException("Stub!");
    }

    public T[] toArray() {
        throw new RuntimeException("Stub!");
    }

    private int indexOf(long cursor) {
        throw new RuntimeException("Stub!");
    }

}