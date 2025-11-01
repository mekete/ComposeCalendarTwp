package com.shalom.calendar.manager;

import android.content.Context;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.shalom.calendar.BuildConfig;
import com.shalom.calendar.R;
import com.shalom.calendar.model.Alarm;
import com.shalom.calendar.model.Model;
import com.shalom.calendar.utility.DeviceUtil;
import com.shalom.calendar.utility.LocaleUtil;

import java.util.Locale;


public class ApplicationManager extends MultiDexApplication {

    private static Context mContext;
    private static Context mBaseContext = null;
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;//"com.shalom.calendar";

    public static Context getAppContext() {
        return mContext;
    }

    private Locale appLocale = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //applyStrictMode();
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
        mContext = getApplicationContext();
        mBaseContext = getBaseContext();
        //
        Model.registerSubclass(Alarm.class);
        addAppOnlyLocaleAndroidApi12();

        //applyDarkMode();
    }

    private void addAppOnlyLocaleAndroidApi12() {
        Configuration config = getBaseContext().getResources().getConfiguration();
        String userSelectedLanguage = SettingManager.getLocaleLanguage();//am,en,fr... to add tg,or,de
        String deviceLanguage = config.locale.getLanguage();

        if (!deviceLanguage.equals(userSelectedLanguage)) {
            //device locale is different from app locale
            //when app launches from landscape mode, it restarts continuously!
            if (!DeviceUtil.isCurrentOrientationLandscape(this)) {
                config.orientation = Configuration.ORIENTATION_PORTRAIT;
                LocaleUtil.setDefaultLocale(mBaseContext, userSelectedLanguage);

            } else {
                Toast.makeText(getBaseContext(), getString(R.string.info_unable_to_show_amharic), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static final String TAG = "ApplicationManager";

    private void applyStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                //.detectDiskReads()
                //.detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .build());


        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectAll()
                .penaltyLog()
                //.penaltyDeath()
                //.detectNonSdkApiUsage()
                .build());
    }

    public static void applyDarkMode() {
        int darkMode = SettingManager.getDarkMode();
        AppCompatDelegate.setDefaultNightMode(darkMode /*AppCompatDelegate.MODE_NIGHT_YES*/);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (appLocale != null) {
            LocaleUtil.setDefaultLocale(mContext, newConfig, appLocale );
        }
    }

    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
}
