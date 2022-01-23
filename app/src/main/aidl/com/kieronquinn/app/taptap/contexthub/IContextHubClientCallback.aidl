package com.kieronquinn.app.taptap.contexthub;

import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient;
import android.hardware.location.NanoAppMessage;

interface IContextHubClientCallback {

    void onHubReset(IRemoteContextHubClient client);
    void onMessageFromNanoApp(IRemoteContextHubClient client, in NanoAppMessage message);
    void onNanoAppAborted(IRemoteContextHubClient client, long nanoAppId, int abortCode);
    void onNanoAppLoaded(IRemoteContextHubClient client, long nanoAppId);

}