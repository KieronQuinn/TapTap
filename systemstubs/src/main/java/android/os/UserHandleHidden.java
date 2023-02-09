package android.os;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(UserHandle.class)
public class UserHandleHidden {

    public int getIdentifier() {
        throw new RuntimeException("Stub!");
    }

}
