package android.content.pm;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;

import java.util.List;

public interface ILauncherApps extends IInterface {

    public static abstract class Stub extends Binder implements ILauncherApps {
        @Override
        public IBinder asBinder() {
            return null;
        }

        public static ILauncherApps asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    ParceledListSlice<ShortcutInfo> getShortcuts(
            String callingPackage,
            ShortcutQueryWrapper query,
            UserHandle user
    );

    @Deprecated
    ParceledListSlice<ShortcutInfo> getShortcuts(
            String callingPackage,
            long changedSince,
            String packageName,
            List<Integer> shortcutIds,
            ComponentName componentName,
            int flags,
            UserHandle user
    );

    boolean startShortcut(
            String callingPackage,
            String packageName,
            String featureId,
            String id,
            Rect sourceBounds,
            Bundle startActivityOptions,
            int userId
    );

    int getShortcutIconResId(
            String callingPackage,
            String packageName,
            String id,
            int userId
    );

    ParcelFileDescriptor getShortcutIconFd(
            String callingPackage,
            String packageName,
            String id,
            int userId
    );

    PendingIntent getShortcutIntent(
            String callingPackage,
            String packageName,
            String shortcutId,
            Bundle opts,
            UserHandle user
    );

    String getShortcutIconUri(
            String callingPackage,
            String packageName,
            String shortcutId,
            int userId
    );

}
