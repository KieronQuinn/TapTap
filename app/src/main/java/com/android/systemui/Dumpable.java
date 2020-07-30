package com.android.systemui;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface Dumpable {
    void dump(FileDescriptor arg1, PrintWriter arg2, String[] arg3);
}

