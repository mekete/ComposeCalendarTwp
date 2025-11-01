package com.shalom.calendar.utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.os.LocaleListCompat;

import com.shalom.calendar.manager.ApplicationManager;

import org.joda.time.Chronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.IslamicChronology;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceUtil {

    public static int getDensityDpi(Context context) {

        return context.getResources().getDisplayMetrics().densityDpi;
    }


    public static int calculateDp(Activity activity) {

        DisplayMetrics displayMetrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrix);
        int smallestPixel = displayMetrix.widthPixels < displayMetrix.heightPixels ? displayMetrix.widthPixels : displayMetrix.heightPixels;
        int smallestDp = activity.getResources().getConfiguration().smallestScreenWidthDp;
        return smallestPixel / smallestDp;
//---------------------------------

//          DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        switch(metrics.densityDpi){
//            case DisplayMetrics.DENSITY_LOW:
//                break;
//            case DisplayMetrics.DENSITY_MEDIUM:
//                break;
//            case DisplayMetrics.DENSITY_HIGH:
//                break;
//        }
        //---------------------------------

//        int pixel = 120;
//        final float scale = getResources().getDisplayMetrics().density;
//        int dip = (int) (pixel* scale + 0.5f);

//        Density can be calculated by the following formula:
//
//        Density = sqrt((wp * wp) + (hp * hp)) / di
//        Where:
//
//        wp is width resolution in pixels,
//
//        hp is height resolution in pixels, and
//
//        di is diagonal size in inches.
    }


    /**
     * Returns total memory accessible by the kernel. This is basically the RAM
     * size of the device. Returns empty if unable to get the value.
     *
     * @return Size of RAM.
     */
    /**
     * Returns total memory accessible by the kernel. This is basically the RAM
     * size of the device. Returns empty if unable to get the value.
     *
     * @return Size of RAM.
     */
    private static String getRamSize(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            return "Total: " + memInfo.totalMem + "  Avail: " + memInfo.availMem;

        } catch (Exception e) {
            return "";
        }
    }

    public static String getAvailableInternalMemorySize(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            return "" + memInfo.availMem;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDeviceInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        StringBuilder builder = new StringBuilder();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            builder.append("versionName  	: ").append(packageInfo.versionName).append("\n");
            builder.append("packageName  	: ").append(packageInfo.packageName).append("\n");
            builder.append("phoneModel   	: ").append(Build.MODEL).append("\n");
            builder.append("androidVersion  : ").append(Build.VERSION.RELEASE).append("\n");
            builder.append("board  			: ").append(Build.BOARD).append("\n");
            builder.append("brand  			: ").append(Build.BRAND).append("\n");
            builder.append("device  		: ").append(Build.DEVICE).append("\n");
            builder.append("display  		: ").append(Build.DISPLAY).append("\n");
            builder.append("fingerPrint  	: ").append(Build.FINGERPRINT).append("\n");
            builder.append("host  			: ").append(Build.HOST).append("\n");
            builder.append("ID  			: ").append(Build.ID).append("\n");
            builder.append("product  		: ").append(Build.PRODUCT).append("\n");
            builder.append("tags  			: ").append(Build.TAGS).append("\n");
            builder.append("TIME  			: ").append(Build.TIME).append("\n");
            builder.append("USER  			: ").append(Build.USER).append("\n");
            builder.append("TYPE  			: ").append(Build.TYPE).append("\n");
            builder.append("RadioVersion    : ").append(Build.getRadioVersion()).append("\n");
            builder.append("AndroidId    	: ").append(getAndroidId(context)).append("\n");
            builder.append("DisplaySize    	: ").append(getDisplaySize(context)).append("\n");
            builder.append("RamSize      	: ").append(getRamSize(context)).append("\n");

            // 
            builder.append("NetworkCountry  : ").append(getNetworkCountyIso(context)).append("\n");
            builder.append("CarrierNetwork  : ").append(getSimOperator(context)).append("\n");
            builder.append("CarrierName     : ").append(getSimOperatorName(context)).append("\n");
            builder.append("TimeZone        : ").append(TimeZone.getDefault().getID()).append("\n");
            builder.append("TimeZoneName    : ").append(TimeZone.getDefault().getDisplayName(Locale.US)).append("\n");
            final Locale currentLocale = getCurrentLocale();
            builder.append("LocaleLanguage  : ").append(currentLocale.getLanguage()).append("\n");
            builder.append("LocaleCountry   : ").append(currentLocale.getCountry()).append("\n");


            //
        } catch (NameNotFoundException e) {
            Log.v("VM", e.getMessage());
        }
        return builder.toString();
    }

    public static Locale getCurrentLocale() {
        return LocaleListCompat.getAdjustedDefault().get(0);
    }

    public static Locale getSuitableSecondaryLocale() {
        String deviceLanguage = DeviceUtil.getCurrentLocale().getLanguage();
        return "fr".equals(deviceLanguage) ? Locale.FRENCH : Locale.ENGLISH;

    }

    public static Chronology getSuitableSecondaryChronology() {
        return isInArabicSpeakingCountry() ? IslamicChronology.getInstance() : GregorianChronology.getInstance();

    }

    public static boolean isInArabicSpeakingCountry() {
        List<String> arabicSpeakingCountries = Arrays.asList(new String[]{ "AE", "BH", "DZ", "EG", "IQ", "IL", "JO", "KW", "LB", "LY", "MA", "OM", "PS", "QA", "SA", "SD", "SY", "TN", "YE"});
        return arabicSpeakingCountries.contains(DeviceUtil.getCurrentLocale().getCountry());
    }

    public static boolean isCurrentOrientationLandscape(Context context) {
        return Configuration.ORIENTATION_LANDSCAPE == context.getResources().getConfiguration().orientation;
    }

    public static int getNoSensorOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);

        Configuration config = context.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public static int getLandscapePixelOrientation(Activity activity, int percentage, boolean fromWidth) {

        DisplayMetrics displayMetrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrix);
        return displayMetrix.widthPixels;
    }


    public static Bitmap getCurrentScreenshot(View view) {
        //assert view.getWidth() > 0 && view.getHeight() > 0;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), config);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    public static ApplicationInfo getApplicationInfo(String packageName) {
        try {
            return ApplicationManager.getAppContext().getPackageManager().getApplicationInfo(packageName, 0);

        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isAppInstalled(String packageName) {
        return getApplicationInfo(packageName) != null;

    }

    public static boolean isAppInstalled(String packageName, boolean checkEnabled) {
        final ApplicationInfo applicationInfo = getApplicationInfo(packageName);
        return !checkEnabled || (applicationInfo != null && applicationInfo.enabled);


    }

    /**
     * Returns the current phone type or returns empty.
     *
     * @return Current phone type
     */
    private String getDeviceType(Context context) {
        try {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int type = telephony.getPhoneType();
            if (type == TelephonyManager.PHONE_TYPE_NONE) {
                return "NONE";
            }
            if (type == TelephonyManager.PHONE_TYPE_CDMA) {
                return "CDMA";
            }
            if (type == TelephonyManager.PHONE_TYPE_GSM) {
                return "GSM";
            }
            if (type == TelephonyManager.PHONE_TYPE_SIP) {
                return "SIP";
            }

        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Returns a 64-bit number (as a hex string) that is randomly generated when
     * the user first sets up the device and should remain constant for the
     * lifetime of the user's device. The value may change if a factory reset is
     * performed on the device. Returns empty if it is unavailable.
     *
     * @return Android id.
     */
    private static String getAndroidId(Context context) {
        return StringUtil.safeTrim(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
    }


    @NonNull
    private static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * Returns screen size small,normal,large,xlarge. Returns empty if unable to get the value.
     *
     * @return screen size i.e small,normal,large,xlarge.
     */
    private static String getScreenSizeMask(Context context) {
        try {
            int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
            if (screenSize == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
                return "UNDEFINED";
            } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                return "LARGE";
            } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_MASK) {
                return "<MASK>";
            } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                return "SMALL";
            } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
                return "NORMAL";
            } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                return "XLARGE";
            }
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Returns the ISO country code equivalent of the current registered ,only when
     * user is registered to a network. Result may be unreliable on CDMA
     * networks.operator's MCC (Mobile Country Code).Returns empty if unavailable.
     *
     * @return The ISO country code
     */
    private static String getNetworkCountyIso(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return StringUtil.safeTrim(tMgr.getNetworkCountryIso());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the numeric name (MCC+MNC) of current registered operator. Only
     * when user is registered to a network. Result may be unreliable on CDMA
     * networks. Returns empty if unavailable.
     *
     * @return Numeric name (MCC+MNC) of current registered operator
     */
    private static String getNetworkOperator(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return StringUtil.safeTrim(tMgr.getNetworkOperator());

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the alphabetic name of current registered operator. Only when user
     * is registered to a network. Result may be unreliable on CDMA networks. Returns empty if unavailable.
     *
     * @return Alphabetic name of current registered operator
     */
    private static String getNetworkOperatorName(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // Result may be unreliable on CDMA networks
            if (tMgr.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                return StringUtil.safeTrim(tMgr.getNetworkOperatorName());
            }
            return StringUtil.safeTrim("CDMA-" + tMgr.getNetworkOperatorName());

        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Return ISO country code equivalent for the SIM provider's country code.Returns empty if unavailable.
     *
     * @return ISO country code equivalent for the SIM provider's country code
     */
    private static String getSimCountryIso(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return StringUtil.safeTrim(tMgr.getSimCountryIso());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns MCC+MNC (mobile country code + mobile network code) of the provider
     * of the SIM. 5 or 6 decimal digits. Returns empty if unavailable.
     *
     * @return MCC+MNC (mobile country code + mobile network code) of the provider of the SIM
     */
    private static String getSimOperator(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return StringUtil.safeTrim(tMgr.getSimOperator());

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the Service Provider Name. Returns empty if unavailable.
     *
     * @return Service Provider Name
     */
    private static String getSimOperatorName(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return StringUtil.safeTrim(tMgr.getSimOperatorName());

        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Returns screen density expressed as dots-per-inch. Returns empty if unable to get the value.
     *
     * @return Screen density expressed as dots-per-inch
     */
    private String getDensity(Context context) {
        try {

            int densityValue = Resources.getSystem().getDisplayMetrics().densityDpi;
            return densityValue + "";
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Return concatenated string of name of the supported instruction set (CPU type + ABI convention).
     * The most preferred ABI is the first element in the string.
     *
     * @return Name of the supported instruction set like:  "arm64-v8a,armeabi-v7a,armeabi"
     */
    private String getCpuAbis() {
        return "";//TextUtils.join(",", Build.SUPPORTED_ABIS);
    }
}
