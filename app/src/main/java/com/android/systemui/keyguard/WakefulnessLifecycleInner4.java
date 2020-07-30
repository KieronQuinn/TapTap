package com.android.systemui.keyguard;

import java.util.function.Consumer;

public final class WakefulnessLifecycleInner4 implements Consumer {
    public static final WakefulnessLifecycleInner4 INSTANCE;

    static {
        INSTANCE = new WakefulnessLifecycleInner4();
    }

    @Override
    public final void accept(Object arg1) {
        ((WakefulnessLifecycle.Observer) arg1).onStartedWakingUp();
    }
}

