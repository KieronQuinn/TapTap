/**
 * Copyright (c) 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.content.pm;

import android.os.UserHandle;
import android.content.pm.ParceledListSlice;
import android.content.pm.ShortcutQueryWrapper;
import android.graphics.Rect;

interface ILauncherApps {

    ParceledListSlice getShortcuts(String callingPackage, in ShortcutQueryWrapper query, in UserHandle user) = 13;
    boolean startShortcut(String callingPackage, String packageName, String featureId, String id, in Rect sourceBounds, in Bundle startActivityOptions, int userId) = 15;
    int getShortcutIconResId(String callingPackage, String packageName, String id, int userId) = 16;
    ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int userId) = 17;
    PendingIntent getShortcutIntent(String callingPackage, String packageName, String shortcutId, in Bundle opts, in UserHandle user) = 22;
    String getShortcutIconUri(String callingPackage, String packageName, String shortcutId, int userId) = 29;

}
