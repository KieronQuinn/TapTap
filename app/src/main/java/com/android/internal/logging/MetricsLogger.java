//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.internal.logging;

import android.content.Context;

public class MetricsLogger {
    public static final int VIEW_UNKNOWN = 0;
    public static final int LOGTAG = 524292;

    public MetricsLogger() {
    }

    public void count(String name, int value) {
    }

    public void histogram(String name, int bucket) {
    }

    public void visible(int category) throws IllegalArgumentException {
    }

    public void hidden(int category) throws IllegalArgumentException {
    }

    public void visibility(int category, boolean visible) throws IllegalArgumentException {
    }

    public void visibility(int category, int vis) throws IllegalArgumentException {
    }

    public void action(int category) {
    }

    public void action(int category, int value) {
    }

    public void action(int category, boolean value) {
    }

    public void action(int category, String pkg) {
    }

    /** @deprecated */
    @Deprecated
    public static void visible(Context context, int category) throws IllegalArgumentException {
    }

    /** @deprecated */
    @Deprecated
    public static void hidden(Context context, int category) throws IllegalArgumentException {
    }

    /** @deprecated */
    @Deprecated
    public static void visibility(Context context, int category, boolean visibile) throws IllegalArgumentException {
    }

    /** @deprecated */
    @Deprecated
    public static void visibility(Context context, int category, int vis) throws IllegalArgumentException {
    }

    /** @deprecated */
    @Deprecated
    public static void action(Context context, int category) {
    }

    /** @deprecated */
    @Deprecated
    public static void action(Context context, int category, int value) {
    }

    /** @deprecated */
    @Deprecated
    public static void action(Context context, int category, boolean value) {
    }

    /** @deprecated */
    @Deprecated
    public static void action(Context context, int category, String pkg) {
    }

    /** @deprecated */
    @Deprecated
    public static void count(Context context, String name, int value) {
    }

    /** @deprecated */
    @Deprecated
    public static void histogram(Context context, String name, int bucket) {
    }
}
