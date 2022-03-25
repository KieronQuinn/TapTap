package com.kieronquinn.app.taptap.shizuku;

import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient;

interface ITapTapShizukuService {

    IRemoteContextHubClient getRemoteContextHubClient() = 1;
    void grantReadLogsPermission() = 2;

    void destroy() = 16777114;

}