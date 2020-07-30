package com.android.systemui.keyguard;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Lifecycle {
    private ArrayList mObservers;

    public Lifecycle() {
        this.mObservers = new ArrayList();
    }

    public void addObserver(Object arg1) {
        this.mObservers.add(arg1);
    }

    public void dispatch(Consumer arg3) {
        int v0;
        for(v0 = 0; v0 < this.mObservers.size(); ++v0) {
            arg3.accept(this.mObservers.get(v0));
        }
    }

    public void removeObserver(Object arg1) {
        this.mObservers.remove(arg1);
    }
}

