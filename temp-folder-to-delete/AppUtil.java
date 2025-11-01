package com.shalom.calendar.utility;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.shalom.calendar.BuildConfig;
import com.shalom.calendar.R;
import com.shalom.calendar.manager.SettingManager;
import com.shalom.calendar.service.SchedulingService;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import java.util.Date;
import java.util.Map;


public class AppUtil {


    public static final int MINIMUM_VERSION_WITH_ENGLISH_SUPPORT = 26;
    public static final int MINIMUM_VERSION_WITH_FIREBASE_TOPIC_SUPPORT = 82;
    public static final int MINIMUM_VERSION_WITH_FRENCH_SUPPORT = 75;
    public static final int MINIMUM_VERSION_WITH_SECONDARY_CHRONOLOGY_SUPPORT = 75;
    public static final int MINIMUM_VERSION_WITH_ADDITIONAL_MUSLIM_HOLIDAY_SUPPORT = 75;

    public static void setupForFirstTime(Activity activity) {
        SettingManager.setCurrentVersion(BuildConfig.VERSION_CODE);
        SettingManager.setFirstTimeAccess(false);
        setupForCurrentVersion(-1, activity);
    }

    public static void setupForCurrentVersion(int previousVersion, Activity activity) {
        SettingManager.setCurrentVersion(BuildConfig.VERSION_CODE);

        if (previousVersion < MINIMUM_VERSION_WITH_SECONDARY_CHRONOLOGY_SUPPORT) {
            SettingManager.setSecondaryLocaleLanguage(DeviceUtil.getSuitableSecondaryLocale().getLanguage());
            SettingManager.setSecondaryChronology(DateTimeUtil.getChronologyAsString(DeviceUtil.getSuitableSecondaryChronology()));
        }
        if (previousVersion < MINIMUM_VERSION_WITH_ADDITIONAL_MUSLIM_HOLIDAY_SUPPORT) {
            if (DeviceUtil.isInArabicSpeakingCountry()) {
                SettingManager.setOrthodoxNameShown(false);
                SettingManager.setOrthodoxOnlyHolidaysShown(false);
                SettingManager.setMuslimOnlyHolidaysShown(true);
            }
        }
        if (previousVersion < MINIMUM_VERSION_WITH_FRENCH_SUPPORT) {
            showLanguagePreferenceDialog(activity);
        }
        if (previousVersion < MINIMUM_VERSION_WITH_FIREBASE_TOPIC_SUPPORT) {
            FirebaseUtil.subscribeToFirebaseTopicIfNeeded(FirebaseUtil.FIREBASE_NOTIFICATION_TOPIC_HOLIDAY_ADJUSTMENT);
            FirebaseUtil.subscribeToFirebaseTopicIfNeeded(FirebaseUtil.FIREBASE_NOTIFICATION_TOPIC_APP_VERSION_UPGRADE);
            FirebaseUtil.subscribeToFirebaseTopicIfNeeded(FirebaseUtil.FIREBASE_NOTIFICATION_TOPIC_SALES_AND_PROMOTION);
            FirebaseUtil.subscribeToFirebaseTopicIfNeeded(FirebaseUtil.FIREBASE_NOTIFICATION_TOPIC_EVENT);
            FirebaseUtil.subscribeToFirebaseTopicIfNeeded(FirebaseUtil.FIREBASE_NOTIFICATION_TOPIC_ALL);
        }

    }

    public static void showLanguagePreferenceDialog(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        final String currentAppLanguage = SettingManager.getLocaleLanguage();
        alertDialogBuilder
                .setTitle(activity.getString(R.string.app_launch_language))
                .setSingleChoiceItems(R.array.pref_language_display_list, 0, null)
                .setPositiveButton(activity.getString(R.string.app_launch_ok),
                        (dialogInterface, i) -> {
                            int selectedPosition = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                            String selectedLanguage = activity.getResources().getStringArray(R.array.pref_language_value_list)[selectedPosition];
                            SettingManager.setLocaleLanguage(selectedLanguage);
                            FirebaseUtil.logSelectContentEvent(activity, FirebaseUtil.USER_ACTION_CATEGORY_CHANGE_SETTINGS, "Language", selectedLanguage, FirebaseUtil.USER_ACTION_CAUSE_APP_PROMPT);

                            if (!currentAppLanguage.equals(selectedLanguage)) {
                                FirebaseUtil.logSelectContentEvent(activity, FirebaseUtil.USER_ACTION_CATEGORY_APP_RESTART, "AppRestarted", currentAppLanguage + "-to-" + selectedLanguage, FirebaseUtil.USER_ACTION_CAUSE_APP_PROMPT);
                                restartAppWithNewLanguageConfiguration(selectedLanguage, activity);
                            }
                        }
                );

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static void restartAppWithNewLanguageConfiguration(String newLanguage, Activity activity) {
        LocaleUtil.setDefaultLocale(activity, newLanguage);
        //wait few milliseconds so that the activity gets properly resumed and then ready tobe killed.
        Handler handler = new Handler();
        //Toast.makeText(this, getString(R.string.info_changing_language), Toast.LENGTH_SHORT).show();
        handler.postDelayed(() -> activity.recreate(), 100);
    }

    public static int getUpdateCurrentIntentFlags() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? (PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE) : (PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void saveHolidayDateAdjustmentFromMap(Map<String, String> data) {

        try {
            int holidayId = Integer.parseInt(data.get("holiday_id"));
            int ethiopianYear = Integer.parseInt(data.get("ethiopian_year"));
            int addition = Integer.parseInt(data.get("addition"));

            SettingManager.setAdditionalDayForHoliday(holidayId, ethiopianYear, addition);
        } catch (NumberFormatException e) {

            FirebaseCrashlytics.getInstance().log(StringUtil.getExceptionAsString(e, -1));
        }

    }

    public static void showPromotionNotification(Map<String, String> data, boolean showNotification, Context context) {
        String externalUrl = data.get("url_string") + "";
        String contentTitle = data.get("content_title") + "";
        String detailText = data.get("detail_text") + "";
        if (showNotification && StringUtil.isValidUrl(externalUrl)) {
            SchedulingService.sendNotification(contentTitle, detailText, SchedulingService.NotificationChannelOption.CHANNEL_SALES_AND_PROMOTION, null, externalUrl, context);
        }
    }

    public static void showUpdateAppDialogIfNeeded(final Activity activity) {
        final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        long lastUpdateCheckTime = SettingManager.getUpdateAvailabilityCheckedTime();
        DateTime current = new DateTime();
        if (current.minusWeeks(1).getMillis() > lastUpdateCheckTime) {
            SettingManager.setUpdateAvailabilityCheckedTime(new DateTime().getMillis());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

            alertDialogBuilder.setMessage(activity.getString(R.string.update_app))
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(R.string.action_update), (dialogInterface, i) -> {
                        FirebaseUtil.logSelectContentEvent(activity, FirebaseUtil.USER_ACTION_CATEGORY_UPDATE_APP, "OpenPlayStore", "OK", FirebaseUtil.USER_ACTION_CAUSE_APP_PROMPT);
                        SocialUtil.shareGooglePlay(activity, null);
                    });
            alertDialogBuilder.setNegativeButton(activity.getString(R.string.action_cancel), (dialog, id) -> {
                FirebaseUtil.logSelectContentEvent(activity, FirebaseUtil.USER_ACTION_CATEGORY_UPDATE_APP, "OpenPlayStore", "Cancel", FirebaseUtil.USER_ACTION_CAUSE_APP_PROMPT);
                dialog.cancel();
            });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }


    public static void saveAppVersionUpgradeFromMap(Map<String, String> data, boolean showNotification, Context context) {
        saveAppVersionUpgradeFromMap(GsonUtil.GSON_INSTANCE.toJson(data), showNotification, context);


    }

    public static void saveAppVersionUpgradeFromMap(String versionJson, boolean showNotification, Context context) {

        try {
            AppVersion version = GsonUtil.GSON_INSTANCE.fromJson(versionJson, AppVersion.class);
            System.out.println(version);
            int releasedVersion = Integer.parseInt(version.releasedVersion);


            if (BuildConfig.VERSION_CODE < releasedVersion) {

                SettingManager.setUpdateAvailableForThisVersion(true);
                SettingManager.setUpdateAvailabilityCheckedTime(new Date().getTime());
                SettingManager.setAppVersionUpgrade(versionJson);

                if (showNotification) {//the context is from service
                    String externalUrl = "market://details?id=" + Constants.RELEASE_VERSION_APPLICATION_ID;
                    String contentTitle = "Update Available for Calendar App";
                    String detailText = "Click update button\n" + "Version : " + version.releasedVersion;
                    SchedulingService.sendNotification(contentTitle, detailText, SchedulingService.NotificationChannelOption.CHANNEL_APP_UPDATE_AVAILABLE, null, externalUrl, context);

                } else {//it can be parsed to activity
                    //show dialog
                }

            } else {
                SettingManager.setUpdateAvailabilityCheckedTime(new Date().getTime());
                SettingManager.setUpdateAvailableForThisVersion(false);
            }

        } catch (NumberFormatException e) {

            FirebaseCrashlytics.getInstance().log(StringUtil.getExceptionAsString(e, -1));
        }

    }

    private static final long MAXIMUM_YEAR_WITHOUT_UPDATE = new DateTime(GregorianChronology.getInstance()).withDate(2030, 1, 1).getMillis();

    public static void checkForUpdateViaSettings(final Activity activity) {
        if (new Date().getTime() >= MAXIMUM_YEAR_WITHOUT_UPDATE) {
            AppUtil.showUpdateAppDialogIfNeeded(activity);
        }

        final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        if (SettingManager.isUpdateAvailableForThisVersion()) {
            try {
                AppVersion appVersion = GsonUtil.GSON_INSTANCE.fromJson(SettingManager.getAppVersionUpgrade(), AppVersion.class);//snapshot.toObject(AppVersion.class);
                long durationSinceLastDisplayed = new Date().getTime() - SettingManager.getUpdateDeclinedDateForThisVersion();
                int releasedVersion = Integer.parseInt(appVersion.releasedVersion);
                if (releasedVersion > BuildConfig.VERSION_CODE) {
                    boolean dismissibleDialog = !AppVersion.UpdateLevel.Critical.toString().equals(appVersion.updateLevel);
                    boolean enoughTimeForBigFeature = (durationSinceLastDisplayed > 7 * DateTimeUtil.DAY_IN_MILLI) && (AppVersion.UpdateLevel.Critical.toString().equals(appVersion.updateLevel) || (AppVersion.UpdateLevel.BigFeature.toString().equals(appVersion.updateLevel)));
                    boolean enoughTimeForMinor = (durationSinceLastDisplayed > 30 * DateTimeUtil.DAY_IN_MILLI) && ((AppVersion.UpdateLevel.MinorUpgrade.toString().equals(appVersion.updateLevel)));
                    if (enoughTimeForBigFeature || enoughTimeForMinor) {
                        showUpgradeDialog(activity, activity.getString(R.string.app_update_available_major), appVersion.updateSummary, dismissibleDialog);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private static void showUpgradeDialog(final Activity activity, String updateTitle, final String updateSummary, boolean hideCancelButton) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder
                .setTitle(updateTitle)
                .setMessage(updateSummary)
                .setPositiveButton(activity.getString(R.string.action_update),
                        (dialogInterface, i) -> SocialUtil.shareGooglePlay(activity, activity.getString(R.string.app_update_toast_message))
                );
        //if (!hideCancelButton)
        {
            alertDialogBuilder.setNegativeButton(activity.getString(R.string.action_do_it_next_time), (dialog, id) -> {
                final long time = new Date().getTime();
                FirebaseUtil.logSelectContentEvent(activity, FirebaseUtil.USER_ACTION_CATEGORY_CHANGE_SETTINGS, "UpdateDecline", "" + time, FirebaseUtil.USER_ACTION_CAUSE_APP_PROMPT);
                SettingManager.setUpdateDeclinedDateForThisVersion(time);
                dialog.cancel();
            });
        }
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static final class AppVersion {

        public static final String APP_VERSION_UPGRADE = "AppVersionUpgrade";

        private static final String TABLE_NAME = "AppVersion";


        public enum UpdateLevel {
            Critical, MinorUpgrade, BigFeature, NewFeature
        }

        String releasedVersion;
        String releasedVersionCode;//
        String releasedVersionName;//= data.get("app_version_name");

        String updateLevel;//= data.get("update_level");//Critical,Minor,BigVersion, Point
        String updateSummary;//= data.get("update_summary");
        String releaseChannel;// = data.get("release_channel");
        String releaseDate;//= data.get("release_date");
        String notificationDate;//= DateTimeUtil.currentDateTimeAsString();

        String previouslyReleasedVersion;//= Integer.parseInt(data.get("app_version_code"));
        String previouslyReleasedMajorVersion;// = Integer.parseInt(data.get("app_version_code"));

        String forceUpdateOsVersions;//= data.get("forced_os_versions");
        String forceUpdateDeviceModels;//= data.get("forced_device_models");
        String forceUpdateAppVersions;//= data.get("forced_device_models");

        //


        @Override
        public String toString() {
            return GsonUtil.GSON_INSTANCE.toJson(this);
        }

    }

}
