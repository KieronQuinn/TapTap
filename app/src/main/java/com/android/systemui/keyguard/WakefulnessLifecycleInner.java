package com.android.systemui.keyguard;

import java.util.function.Consumer;

public final class WakefulnessLifecycleInner implements Consumer {
    public static final WakefulnessLifecycleInner INSTANCE;

    static {
        INSTANCE = new WakefulnessLifecycleInner();
    }

    @Override
    public final void accept(Object arg1) {
        ((WakefulnessLifecycle.Observer) arg1).onFinishedGoingToSleep();
    }
}

