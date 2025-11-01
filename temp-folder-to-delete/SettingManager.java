package com.shalom.calendar.manager;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.shalom.calendar.BuildConfig;
import com.shalom.calendar.utility.Constants;
import com.shalom.calendar.utility.DateTimeUtil;
import com.shalom.calendar.utility.DeviceUtil;

import org.joda.time.Chronology;

import java.util.Locale;
import java.util.TimeZone;

public class SettingManager {//implements Preference.OnPreferenceChangeListener{

    //
    public static final String KEY_SECONDARY_LOCALE = "pref_key_secondary_locale";
    public static final String KEY_SECONDARY_CHRONOLOGY = "pref_key_chronology_secondary";
    private static final String KEY_HIDE_SECONDARY_TIME_IN_WIDGET = "pref_key_hide_secondary_widget";
    private static final String KEY_WIDGET_COLOR_TRANSPARENCY = "pref_key_widget_transparency";
    private static final String KEY_USE_TRANSPARENT_BACKGROUND_FOR_WIDGET = "pref_key_use_dark_background_for_widgets";
    private static final String KEY_USER_PREFERENCE_OPENED_COUNT = "pref_key_user_preference_opened_count";


    public static final String TIME_ZONE_AFRICA_ADDIS_ABABA = "Africa/Addis_Ababa";
    private static final Context context = ApplicationManager.getAppContext();
    //
    //these constant values should be same as that of pref_content.xml values
    private static final String KEY_CURRENT_VERSION = "current_version";


    public static final String pref_ref_dark_mode_light = "light";
    public static final String pref_ref_language_code_fr = "fr";

    private static final String KEY_DARK_MODE = "pref_key_dark_mode";
    private static final String KEY_UPDATE_DECLINED_TIME = "pref_key_update_differed_time";

    private static final String KEY_LATEST_APP_USED_TIME = "pref_key_latest_app_used_time";
    private static final String KEY_LATEST_UPDATE_AVAILABILITY_TIME = "pref_key_latest_update_availability_checked_time";
    private static final String KEY_LATEST_ALARM_USED_TIME = "pref_key_latest_alarm_used_time";
    private static final String KEY_FIRST_TIME_ACCESS = "pref_key_first_time_access";
    private static final String KEY_BIRTH_DAY = "pref_key_birth_day";
    //
    public static final String KEY_GEEZ_NUMBER_SHOWN = "pref_key_geez_number_shown";
    public static final String KEY_PRIMARY_LOCALE = "pref_key_primary_locale";

    public static final String KEY_ORTHODOX_DAY_NAME_SHOWN = "pref_key_orthodox_day_name_shown";

    private static final String KEY_ORTHODOX_ONLY_HOLIDAYS_SHOWN = "pref_key_orthodox_only_holiday_shown";
    private static final String KEY_MUSLIM_ONLY_HOLIDAYS_SHOWN = "pref_key_muslim_only_holiday_shown";
    private static final String KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_ID = "pref_key_today_widget_primary_time_zone";

    private static final String KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_NAME = "pref_key_today_widget_primary_time_zone_name";

    private static final String KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_ID = "pref_key_today_widget_secondary_time_zone";

    private static final String KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_NAME = "pref_key_today_widget_secondary_time_zone_NAME";

    private static final String KEY_24_HOUR_FORMAT_FOR_TIME_PICKER = "pref_key_24_hour_format_for_time_picker";

    public static final String LOCALE_AMHARIC = Constants.LOCALE_AMHARIC;//"am";

    private static String getStringResourceById(int id) {
        return context.getResources().getString(id);
    }

    public static void setBirthDate(String value) {

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_BIRTH_DAY, value).commit();
    }

    public static String getBirthDate() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_BIRTH_DAY, "");

    }

    public static boolean isGeezNumberShown() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_GEEZ_NUMBER_SHOWN, false);
    }


    // -------------------------------------------------------------------------

    public static boolean isOrthodoxNameShown() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ORTHODOX_DAY_NAME_SHOWN, true);
    }

    public static void setOrthodoxNameShown(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_ORTHODOX_DAY_NAME_SHOWN, value).commit();
    }

    public static boolean isOrthodoxOnlyHolidaysShown() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ORTHODOX_ONLY_HOLIDAYS_SHOWN, false);
    }

    public static void setOrthodoxOnlyHolidaysShown(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_ORTHODOX_ONLY_HOLIDAYS_SHOWN, value).commit();
    }


    public static boolean isMuslimOnlyHolidaysShown() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_MUSLIM_ONLY_HOLIDAYS_SHOWN, false);
    }

    public static void setMuslimOnlyHolidaysShown(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_MUSLIM_ONLY_HOLIDAYS_SHOWN, value).commit();
    }


    public static String getSecondaryClockWidgetZone() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_ID, "");
    }

    public static void setSecondaryClockWidgetZone(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_ID, value).commit();
    }


    public static boolean isUse24HourFormatForTime() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_24_HOUR_FORMAT_FOR_TIME_PICKER, false);
    }

    public static void setUse24HourFormatForTime(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_24_HOUR_FORMAT_FOR_TIME_PICKER, value).commit();
    }


    public static boolean isFirstTimeAccess() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_FIRST_TIME_ACCESS, true);
    }

    public static void setFirstTimeAccess(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_FIRST_TIME_ACCESS, value).commit();
    }


    // -------------------------------------------------------------------------
    public static void setLocaleLanguage(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_PRIMARY_LOCALE, value).commit();
    }

    private static final String TAG = "SettingManager";
    
    
    public static String getLocaleLanguage() {



        String secondaryLocale = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PRIMARY_LOCALE, LOCALE_AMHARIC);
        Log.e(TAG, "getLocaleLanguage(): secondaryLocale >>>>>>>>>" +
                "\n>> "+secondaryLocale +
                "\n>> KEY_PRIMARY_LOCALE:    :::  "+KEY_PRIMARY_LOCALE+
                "\n>> LOCALE_AMHARIC :::  "+LOCALE_AMHARIC  +
                "\n>> >>>>>>FAKE" +
                 "\n>> KEY_PRIMARY_LOCALE FAKE:    :::  " );
        return secondaryLocale;

    }

    // -------------------------------------------------------------------------
    public static long getLatestAppUsedTime() {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_LATEST_APP_USED_TIME, -1);
    }

    public static void setLatestAppUsedTime(long value) {

        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_LATEST_APP_USED_TIME, value).commit();
    }

    public static long getUpdateAvailabilityCheckedTime() {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_LATEST_UPDATE_AVAILABILITY_TIME, -1);
    }

    public static void setUpdateAvailabilityCheckedTime(long value) {

        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_LATEST_UPDATE_AVAILABILITY_TIME, value).commit();
    }

    // -------------------------------------------------------------------------
    public static long getLatestAlarmUsedTime() {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_LATEST_ALARM_USED_TIME, -1);
    }

    public static void setLatestAlarmUsedTime(long value) {

        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_LATEST_ALARM_USED_TIME, value).commit();
    }

    // -------------------------------------------------------------------------
    public static void setLastTimeAccessed(String category, String time) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(category, time).commit();
    }

    public static String getLastTimeAccessed(String category) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(category, "Not opened yet!");

    }

    public static int getCurrentVersion() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_CURRENT_VERSION, 0);
    }

    public static void setCurrentVersion(int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_CURRENT_VERSION, value).commit();
    }

    public static int getDarkMode() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static void setDarkMode(int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_DARK_MODE, value).commit();
    }


    public static void setUpdateDeclinedDateForThisVersion(long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_UPDATE_DECLINED_TIME + "_" + BuildConfig.VERSION_CODE, value).commit();
    }

    public static long getUpdateDeclinedDateForThisVersion() {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_UPDATE_DECLINED_TIME + "_" + BuildConfig.VERSION_CODE, 0L);
    }

    public static void setAdditionalDayForHoliday(int holidayId, int ethiopianYear, int addition) {
        String holidayAndEthiopianYearKey = "HolidayDateAdjustment" + "__" + holidayId + "__" + ethiopianYear;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(holidayAndEthiopianYearKey, addition).commit();
    }

    public static int getAdditionalDayForHoliday(int holidayId, int ethiopianYear) {
        String holidayAndEthiopianYearKey = "HolidayDateAdjustment" + "__" + holidayId + "__" + ethiopianYear;
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(holidayAndEthiopianYearKey, 0);

    }

    public static void setAppVersionUpgrade(String appVersionJsonInfo) {
         PreferenceManager.getDefaultSharedPreferences(context).edit().putString("VERSION_UPGRADE_INFO", appVersionJsonInfo).commit();
    }

    public static String getAppVersionUpgrade() {
         return PreferenceManager.getDefaultSharedPreferences(context).getString("VERSION_UPGRADE_INFO", "");

    }

    public static void setSubscribedToFirebaseTopic(String topic, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("SUBSCRIBED_TO_FIREBASE_TOPIC_" + topic, value).commit();

    }

    public static boolean isHolidayNotificationShown(int ethiopianYear , int ethiopianMonth,int holidayId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("HOLIDAY_NOTIFIED_" + ethiopianYear+"_"+ethiopianMonth+"_"+holidayId, false);
    }
    public static void setHolidayNotificationShown(int ethiopianYear, int ethiopianMonth , int holidayId, boolean value) {
          PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("HOLIDAY_NOTIFIED_" + ethiopianYear+"_"+ethiopianMonth+"_"+holidayId, value).commit();
    }

    public static boolean isSubscribedToFirebaseTopic(String topic) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("SUBSCRIBED_TO_FIREBASE_TOPIC_" + topic, false);
    }

    public static void setUpdateAvailableForThisVersion(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("UPDATE_AVAILABLE_TO_VERSION_" + BuildConfig.VERSION_CODE, value).commit();

    }

    public static boolean isUpdateAvailableForThisVersion() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("UPDATE_AVAILABLE_TO_VERSION_" + BuildConfig.VERSION_CODE, false);

    }

    public static void setFirebaseToken(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("FIREBASE_TOKEN", value).commit();
    }

    public static String getFirebaseToken() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("FIREBASE_TOKEN", "");

    }

    public static void setFirebaseInstallationId(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("FIREBASE_INSTALLATION_ID", value).commit();
    }

    public static String getFirebaseInstallationId() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("FIREBASE_INSTALLATION_ID", "");

    }

    public static Locale getSecondaryLocale() {
         //return  new Locale(secondaryLocaleLanguage);
        return new Locale(fetchAndUpdateSecondaryLocaleLanguage());
    }

    private static final String secondaryLocaleLanguage = fetchAndUpdateSecondaryLocaleLanguage();


    public static String getSecondaryLocaleLanguage() {
        return secondaryLocaleLanguage;
    }
    public static String fetchAndUpdateSecondaryLocaleLanguage() {
        String primaryLocaleLanguage = SettingManager.getLocaleLanguage();
        if ("am".equals(primaryLocaleLanguage)) {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_SECONDARY_LOCALE, DeviceUtil.getSuitableSecondaryLocale().getLanguage());
        }

        Log.e(TAG, ">>>fetchAndUpdateSecondaryLocaleLanguage():  LocaleIssue >>>>>>>>>" +
                "\n>> primaryLocaleLanguage:  "+primaryLocaleLanguage +
                "\n>> DeviceUtil.getSuitableSecondaryLocale().getLanguage():: "+DeviceUtil.getSuitableSecondaryLocale().getLanguage() +
                "\n>> KEY_SECONDARY_LOCALE:: "+KEY_SECONDARY_LOCALE );


        return primaryLocaleLanguage;
    }

    public static void setSecondaryLocaleLanguage(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_SECONDARY_LOCALE, value).commit();
    }

    public static void setSecondaryChronology(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_SECONDARY_CHRONOLOGY, value).commit();
    }

    public static Chronology getSecondaryChronology() {
        String secondary = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_SECONDARY_CHRONOLOGY, DateTimeUtil.CHRONOLOGY_GREGORIAN);
        return DateTimeUtil.getChronologyFromString(secondary);
    }



    public static String getTodayWidgetPrimaryTimeZoneName() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_NAME, "");
    }

    public static void setTodayWidgetPrimaryTimeZoneName(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_NAME, value).commit();
    }


    public static String getTodayWidgetPrimaryTimeZoneId() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_ID, TimeZone.getDefault().getID());
    }

    public static void setTodayWidgetPrimaryTimeZoneId(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TODAY_WIDGET_PRIMARY_TIME_ZONE_ID, value).commit();
    }




    public static String getTodayWidgetSecondaryTimeZoneId() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_ID, TIME_ZONE_AFRICA_ADDIS_ABABA);
    }

    public static void setTodayWidgetSecondaryTimeZoneId(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_ID, value).commit();
    }

    public static String getTodayWidgetSecondaryTimeZoneName() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_NAME, "");
    }

    public static void setTodayWidgetSecondaryTimeZoneName(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TODAY_WIDGET_SECONDARY_TIME_ZONE_NAME, value).commit();
    }



    public static boolean isSecondaryTimeWidgetHidden() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_HIDE_SECONDARY_TIME_IN_WIDGET, false);
    }

    public static void setSecondaryTimeWidgetHidden(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_HIDE_SECONDARY_TIME_IN_WIDGET, value).commit();
    }

    public static boolean isUseTransparentBackgroundForWidgets() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_USE_TRANSPARENT_BACKGROUND_FOR_WIDGET, true);
    }

    public static void setUseTransparentBackgroundForWidgets(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_USE_TRANSPARENT_BACKGROUND_FOR_WIDGET, value).commit();
    }

    public static int getWidgetColorTransparency() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_WIDGET_COLOR_TRANSPARENCY, 70);
    }

    public static void setWidgetColorTransparency(int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_WIDGET_COLOR_TRANSPARENCY, value).commit();
    }

    public static int getUserOpenedIntentCount() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_USER_PREFERENCE_OPENED_COUNT, 0);
    }

    public static void updateUserOpenedIntentCount() {
        int count=PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_USER_PREFERENCE_OPENED_COUNT, 0);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_USER_PREFERENCE_OPENED_COUNT, count++).commit();
    }


    // -------------------------------------------------------------------------

    public static String getLastHolidayNotifiedDate() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("LastHolidayNotifiedDate", "2000_01_01");
    }

    public static void setLastHolidayNotifiedDate(String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("LastHolidayNotifiedDate", value).commit();
    }
}
