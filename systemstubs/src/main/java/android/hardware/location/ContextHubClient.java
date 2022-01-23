/*
 * Copyright 2017 The Android Open Source Project
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
import androidx.annotation.RequiresPermission;
import android.app.PendingIntent;
import android.os.RemoteException;
import android.util.Log;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class describing a client of the Context Hub Service.
 *
 * Clients can send messages to nanoapps at a Context Hub through this object. The APIs supported
 * by this object are thread-safe and can be used without external synchronization.
 *
 * @hide
 */
public class ContextHubClient implements Closeable {

    /**
     * Returns the hub that this client is attached to.
     *
     * @return the ContextHubInfo of the attached hub
     */
    @NonNull
    public ContextHubInfo getAttachedHub() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Closes the connection for this client and the Context Hub Service.
     *
     * When this function is invoked, the messaging associated with this client is invalidated.
     * All futures messages targeted for this client are dropped at the service, and the
     * ContextHubClient is unregistered from the service.
     *
     * If this object has a PendingIntent, i.e. the object was generated via
     * {@link `ContextHubManager.createClient(PendingIntent, ContextHubInfo, long)}`, then the
     * Intent events corresponding to the PendingIntent will no longer be triggered.
     */
    public void close() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sends a message to a nanoapp through the Context Hub Service.
     *
     * This function returns RESULT_SUCCESS if the message has reached the HAL, but
     * does not guarantee delivery of the message to the target nanoapp.
     *
     * Before sending the first message to your nanoapp, it's recommended that the following
     * operations should be performed:
     * 1) Invoke {@link `ContextHubManager#queryNanoApps(ContextHubInfo)`} to verify the nanoapp is
     *    present.
     * 2) Validate that you have the permissions to communicate with the nanoapp by looking at
     *    {@link `NanoAppState#getNanoAppPermissions`}.
     * 3) If you don't have permissions, send an idempotent message to the nanoapp ensuring any
     *    work your app previously may have asked it to do is stopped. This is useful if your app
     *    restarts due to permission changes and no longer has the permissions when it is started
     *    again.
     * 4) If you have valid permissions, send a message to your nanoapp to resubscribe so that it's
     *    aware you have restarted or so you can initially subscribe if this is the first time you
     *    have sent it a message.
     *
     * @param message the message object to send
     *
     * @return the result of sending the message defined as in ContextHubTransaction.Result
     *
     * @throws NullPointerException if NanoAppMessage is null
     * @throws SecurityException if this client doesn't have permissions to send a message to the
     * nanoapp.
     *
     * @see `NanoAppMessage`
     * @see `ContextHubTransaction.Result`
     */
    @RequiresPermission(anyOf = {
            android.Manifest.permission.LOCATION_HARDWARE,
            //android.Manifest.permission.ACCESS_CONTEXT_HUB
    })
    public int sendMessageToNanoApp(@NonNull NanoAppMessage message) {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected void finalize() throws Throwable {
        throw new RuntimeException("Stub!");
    }

}