package com.peritoflores.androidremoteforensics.ahm;


import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.peritoflores.androidremoteforensics.MainActivity;


public class DangerousAppUtils {
Context ctx;
    static final String TAG = "DangerousAppUtils";



public void HideAppIcon() {
    //Hide Icon
    PackageManager p = ctx.getPackageManager();
    ComponentName componentName = new ComponentName(ctx, MainActivity.class);
    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

}

    public DangerousAppUtils(Context ctx){

    this.ctx=ctx;
}
}
