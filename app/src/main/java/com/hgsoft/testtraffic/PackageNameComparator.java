package com.hgsoft.testtraffic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Comparator;

/**
 * Created by YUDAPEI on 17/1/15.
 */

public class PackageNameComparator implements Comparator<PackageInfo> {
    PackageManager pm;

    public PackageNameComparator(PackageManager pm) {
        this.pm = pm;
    }

    @Override
    public int compare(PackageInfo lhs, PackageInfo rhs) {
        return lhs.applicationInfo.loadLabel(pm).toString().compareTo(rhs.applicationInfo.loadLabel(pm).toString());
    }
}
