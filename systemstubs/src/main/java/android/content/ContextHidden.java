package android.content;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.os.Handler;
import android.os.UserHandle;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(Context.class)
public class ContextHidden {

    public UserHandle getUser() {
        throw new RuntimeException("Stub!");
    }

    public Handler getMainThreadHandler() {
        throw new RuntimeException("Stub!");
    }

    public IApplicationThread getIApplicationThread() {
        throw new RuntimeException("Stub!");
    }

    public IServiceConnection getServiceDispatcher(ServiceConnection connection, Handler handler, int flags) {
        throw new RuntimeException("Stub!");
    }

}
