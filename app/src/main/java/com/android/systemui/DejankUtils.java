package com.android.systemui;

import java.util.function.Supplier;

public class DejankUtils {

    public static Object whitelistIpcs(Supplier supplier){
        return supplier.get();
    }

}
