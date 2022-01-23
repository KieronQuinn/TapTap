package com.kieronquinn.app.taptap.contexthub;

import android.hardware.location.NanoAppMessage;
import com.kieronquinn.app.taptap.contexthub.IContextHubClientCallback;

interface IRemoteContextHubClient {

    int sendMessageToNanoApp(IContextHubClientCallback callback, in NanoAppMessage message);

}