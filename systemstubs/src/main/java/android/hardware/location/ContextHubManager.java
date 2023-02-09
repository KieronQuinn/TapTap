package android.hardware.location;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

public class ContextHubManager {

    /**
     * Constants describing if a {@link ContextHubClient} and a {@link `NanoApp`} are authorized to
     * communicate.
     *
     * @hide
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            AUTHORIZATION_DENIED,
            AUTHORIZATION_DENIED_GRACE_PERIOD,
            AUTHORIZATION_GRANTED,
    })
    public @interface AuthorizationState { }

    /**
     * Indicates that the {@link ContextHubClient} can no longer communicate with a nanoapp. If the
     * {@link ContextHubClient} attempts to send messages to the nanoapp, it will continue to
     * receive this authorization state if the connection is still closed.
     */
    public static final int AUTHORIZATION_DENIED = 0;

    /**
     * Indicates the {@link ContextHubClient} will soon lose its authorization to communicate with a
     * nanoapp. After receiving this state event, the {@link ContextHubClient} has one minute to
     * perform any cleanup with the nanoapp such that the nanoapp is no longer performing work on
     * behalf of the {@link ContextHubClient}.
     */
    public static final int AUTHORIZATION_DENIED_GRACE_PERIOD = 1;

    /**
     * The {@link ContextHubClient} is authorized to communicate with the nanoapp.
     */
    public static final int AUTHORIZATION_GRANTED = 2;

    /**
     * Returns the list of ContextHubInfo objects describing the available Context Hubs.
     *
     * @return the list of ContextHubInfo objects
     *
     * @see ContextHubInfo
     */
    @RequiresPermission(anyOf = {
            android.Manifest.permission.LOCATION_HARDWARE,
            //android.Manifest.permission.ACCESS_CONTEXT_HUB
    })
    @NonNull
    public List<ContextHubInfo> getContextHubs() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Equivalent to {@link #`createClient(ContextHubInfo, ContextHubClientCallback, Executor)`}
     * with the executor using the main thread's Looper.
     */
    @RequiresPermission(anyOf = {
            android.Manifest.permission.LOCATION_HARDWARE,
            //android.Manifest.permission.ACCESS_CONTEXT_HUB
    })
    @NonNull public ContextHubClient createClient(
            @NonNull ContextHubInfo hubInfo, @NonNull ContextHubClientCallback callback) {
        throw new RuntimeException("Stub!");
    }

    @RequiresPermission(anyOf = {
            android.Manifest.permission.LOCATION_HARDWARE,
            //android.Manifest.permission.ACCESS_CONTEXT_HUB
    })
    public ContextHubClient createClient(Context context, ContextHubInfo hubInfo, Executor executor, ContextHubClientCallback callback) {
        throw new RuntimeException("Stub!");
    }

    @NonNull public ContextHubTransaction<Void> loadNanoApp(
            @NonNull ContextHubInfo hubInfo, @NonNull NanoAppBinary appBinary) {
        throw new RuntimeException("Stub!");
    }

    @NonNull public ContextHubTransaction<Void> unloadNanoApp(
            @NonNull ContextHubInfo hubInfo, long nanoAppId) {
        throw new RuntimeException("Stub!");
    }

}
