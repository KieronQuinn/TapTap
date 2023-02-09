package android.app;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(ActivityManager.class)
public class ActivityManagerHidden {

    public static boolean isHighEndGfx() {
        throw new RuntimeException("Stub!");
    }

}