package com.kieronquinn.app.taptap.shizuku;

import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutIcon;
import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient;
import android.os.UserHandle;
import android.content.pm.ShortcutQueryWrapper;
import android.content.pm.ParceledListSlice;
import com.kieronquinn.app.taptap.models.service.SnapchatQuickTapState;

interface ITapTapShizukuService {

    IRemoteContextHubClient getRemoteContextHubClient() = 1;

    ParceledListSlice getShortcuts(in ShortcutQueryWrapper query) = 2;

    AppShortcutIcon getAppShortcutIcon(String packageName, String shortcutId) = 3;

    void startShortcut(String packageName, String shortcutId) = 4;

    void inputKeyEvent(int keyId) = 5;

    void clickQuickSetting(String component) = 6;

    void destroy() = 16777114;

}