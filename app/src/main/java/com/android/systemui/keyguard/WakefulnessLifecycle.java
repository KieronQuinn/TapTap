package com.android.systemui.keyguard;

import android.os.Trace;
import android.util.Log;

import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WakefulnessLifecycle extends Lifecycle implements Dumpable {
    public interface Observer {
        default void onFinishedGoingToSleep() {
        }

        default void onFinishedWakingUp() {
        }

        default void onStartedGoingToSleep() {
        }

        default void onStartedWakingUp() {
        }
    }

    private int mWakefulness;

    public WakefulnessLifecycle() {
        this.mWakefulness = 0;
    }

    public void dispatchFinishedGoingToSleep() {
        if(this.getWakefulness() == 0) {
            return;
        }

        Log.d("Wakefulness", "dispatchFinishedGoingToSleep");

        this.setWakefulness(0);
        this.dispatch(WakefulnessLifecycleInner.INSTANCE);
    }

    public void dispatchFinishedWakingUp() {
        if(this.getWakefulness() == 2) {
            return;
        }

        Log.d("Wakefulness", "dispatchFinishedWakingUp");

        this.setWakefulness(2);
        this.dispatch(WakefulnessLifecycleInner2.INSTANCE);
    }

    public void dispatchStartedGoingToSleep() {
        if(this.getWakefulness() == 3) {
            return;
        }

        Log.d("Wakefulness", "dispatchStartedGoingToSleep");

        this.setWakefulness(3);
        this.dispatch(WakefulnessLifecycleInner3.INSTANCE);
    }

    public void dispatchStartedWakingUp() {
        if(this.getWakefulness() == 1) {
            return;
        }

        Log.d("Wakefulness", "dispatchStartedWakingUp");

        this.setWakefulness(1);
        this.dispatch(WakefulnessLifecycleInner4.INSTANCE);
    }

    @Override  // com.android.systemui.Dumpable
    public void dump(FileDescriptor arg1, PrintWriter arg2, String[] arg3) {
        arg2.println("WakefulnessLifecycle:");
        arg2.println("  mWakefulness=" + this.mWakefulness);
    }

    public int getWakefulness() {
        return this.mWakefulness;
    }

    private void setWakefulness(int arg3) {
        this.mWakefulness = arg3;
        //Trace.traceCounter(0x1000L, "wakefulness", arg3);
    }
}

