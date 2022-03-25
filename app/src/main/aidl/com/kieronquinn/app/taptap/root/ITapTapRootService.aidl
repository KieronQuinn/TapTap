package com.kieronquinn.app.taptap.root;

import com.kieronquinn.app.taptap.models.service.SnapchatQuickTapState;
import com.kieronquinn.app.taptap.models.service.ActivityContainer;

interface ITapTapRootService {
    SnapchatQuickTapState isSnapchatQuickTapToSnapEnabled();
    void applySnapchatOverride();
    void grantReadLogsPermission();

    int startActivityPrivileged(in ActivityContainer activityContainer, in Intent intent);
}