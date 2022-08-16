package com.kieronquinn.app.taptap.shizuku;

import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutIcon;
import com.kieronquinn.app.taptap.models.appshortcut.ShortcutQueryWrapper;
import com.kieronquinn.app.taptap.shizuku.ITapTapColumbusLogCallback;
import android.os.UserHandle;
import android.content.pm.ParceledListSlice;

interface ITapTapShizukuShellService {

    ParceledListSlice getShortcuts(in ShortcutQueryWrapper query) = 1;

    AppShortcutIcon getAppShortcutIcon(String packageName, String shortcutId) = 2;

    void startShortcut(String packageName, String shortcutId) = 3;

    void inputKeyEvent(int keyId) = 4;

    void clickQuickSetting(String component) = 5;

    void setColumbusLogListener(ITapTapColumbusLogCallback callback) = 6;

    void destroy() = 16777114;

}