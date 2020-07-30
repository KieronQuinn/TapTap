package com.kieronquinn.app.taptap.utils;

import dagger.Lazy;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class InstanceFactory implements Lazy, Factory {
    private final Object instance;

    static {
    }

    public InstanceFactory(Object arg1) {
        this.instance = arg1;
    }

    public static Factory create(Object arg2) {
        Preconditions.checkNotNull(arg2, "instance cannot be null");
        return new InstanceFactory(arg2);
    }

    @Override  // dagger.Lazy, javax.inject.Provider
    public Object get() {
        return this.instance;
    }
}

