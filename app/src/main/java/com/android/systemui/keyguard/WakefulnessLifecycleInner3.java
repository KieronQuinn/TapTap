package com.android.systemui.keyguard;

import java.util.function.Consumer;

public final class WakefulnessLifecycleInner3 implements Consumer {
    public static final WakefulnessLifecycleInner3 INSTANCE;

    static {
        INSTANCE = new WakefulnessLifecycleInner3();
    }

    @Override
    public final void accept(Object arg1) {
        ((WakefulnessLifecycle.Observer) arg1).onStartedGoingToSleep();
    }
}

