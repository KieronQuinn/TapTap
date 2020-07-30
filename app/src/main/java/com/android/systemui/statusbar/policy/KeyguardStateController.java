package com.android.systemui.statusbar.policy;

public interface KeyguardStateController extends CallbackController {
    public interface Callback {
        default void onKeyguardFadingAwayChanged() {
        }

        default void onKeyguardShowingChanged() {
        }

        default void onUnlockedChanged() {
        }
    }

    long calculateGoingToFullShadeDelay();

    boolean canDismissLockScreen();

    long getKeyguardFadingAwayDelay();

    long getKeyguardFadingAwayDuration();

    default long getShortenedFadingAwayDuration() {
        return this.isBypassFadingAnimation() ? this.getKeyguardFadingAwayDuration() : this.getKeyguardFadingAwayDuration() / 2L;
    }

    default boolean isBypassFadingAnimation() {
        return false;
    }

    default boolean isFaceAuthEnabled() {
        return false;
    }

    boolean isKeyguardFadingAway();

    boolean isKeyguardGoingAway();

    boolean isLaunchTransitionFadingAway();

    boolean isMethodSecure();

    boolean isOccluded();

    boolean isShowing();

    default boolean isUnlocked() {
        return !this.isShowing() || (this.canDismissLockScreen());
    }

    default void notifyKeyguardDoneFading() {
    }

    default void notifyKeyguardFadingAway(long arg1, long arg3, boolean arg5) {
    }

    default void notifyKeyguardGoingAway(boolean arg1) {
    }

    default void notifyKeyguardState(boolean arg1, boolean arg2) {
    }

    default void setLaunchTransitionFadingAway(boolean arg1) {
    }
}

