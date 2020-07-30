package com.android.systemui.keyguard;

import java.util.function.Consumer;

public final class WakefulnessLifecycleInner2 implements Consumer {
    public static final WakefulnessLifecycleInner2 INSTANCE;

    static {
        INSTANCE = new WakefulnessLifecycleInner2();
    }

    @Override
    public final void accept(Object arg1) {
        ((WakefulnessLifecycle.Observer) arg1).onFinishedWakingUp();
    }
}

