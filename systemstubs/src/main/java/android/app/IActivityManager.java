package android.app;

import android.content.IIntentSender;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

public interface IActivityManager extends android.os.IInterface {

    abstract class Stub extends android.os.Binder implements android.app.IServiceConnection
    {
        public static IActivityManager asInterface(android.os.IBinder obj)
        {
            throw new RuntimeException("Stub!");
        }
    }

    @RequiresApi(29)
    ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token, String tag)
            throws RemoteException;

    @RequiresApi(26)
    ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token)
            throws RemoteException;

    void removeContentProviderExternal(String name, IBinder token)
            throws RemoteException;

    void registerUserSwitchObserver(IUserSwitchObserver observer, String tag);
    void unregisterUserSwitchObserver(IUserSwitchObserver observer);

    Intent getIntentForIntentSender(IIntentSender sender);

    int startActivityWithFeature(
            IApplicationThread caller,
            String callingPackage,
            String callingFeatureId,
            Intent intent,
            String resolvedType,
            IBinder resultTo,
            String resultWho,
            int requestCode,
            int flags,
            ProfilerInfo profilerInfo,
            Bundle options);

    void resumeAppSwitches();

    void forceStopPackage(String packageName, int userId);

}