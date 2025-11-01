package com.shalom.calendar.utility;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.shalom.calendar.R;
import com.shalom.calendar.manager.SettingManager;
import com.shalom.calendar.service.MyFirebaseMessagingService;

public class FirebaseUtil {
    public static final String USER_ACTION_CATEGORY_OPEN_VIEW = "ActionCategoryOpenView";
    public static final String USER_ACTION_CATEGORY_CHANGE_SETTINGS = "ActionCategoryChangeSettings";
    public static final String USER_ACTION_CATEGORY_SOCIAL = "ActionCategorySocial";
    public static final String USER_ACTION_CATEGORY_CONVERT_DATE = "ActionCategoryConvertDate";
    public static final String USER_ACTION_CATEGORY_UPDATE_APP = "ActionCategoryUpdateApp";
    //
    public static final String USER_ACTION_CATEGORY_APP_RESTART = "ActionCategoryAppRestart";


    public static final String USER_ACTION_CAUSE_APP_PROMPT = "ActionCauseAppPrompt";
    public static final String USER_ACTION_CAUSE_USER = "ActionCauseUser";

    public static final String SELECTED_VALUE_UNKNOWN = "Unknown";
    //

    public static final String FIREBASE_NOTIFICATION_TOPIC_HOLIDAY_ADJUSTMENT = MyFirebaseMessagingService.FIREBASE_MESSAGE_CATEGORY_HOLIDAY_DATE_ADJUSTMENT;
    public static final String FIREBASE_NOTIFICATION_TOPIC_APP_VERSION_UPGRADE = MyFirebaseMessagingService.FIREBASE_MESSAGE_CATEGORY_APP_VERSION_UPGRADE;
    public static final String FIREBASE_NOTIFICATION_TOPIC_EVENT = MyFirebaseMessagingService.FIREBASE_MESSAGE_CATEGORY_EVENT;
    public static final String FIREBASE_NOTIFICATION_TOPIC_SALES_AND_PROMOTION = MyFirebaseMessagingService.FIREBASE_MESSAGE_CATEGORY_SALES_AND_PROMOTION;
    public static final String FIREBASE_NOTIFICATION_TOPIC_ALL = "TopicAllTest";

    public static void logSelectContentEvent(Context context , String actionCategory, String actionName, String value, String actionCause) {
        FirebaseAnalytics firebaseAnalytics=FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString("actionCategory", actionCategory);//share, navigate, open app, open view, setting lang-amh
        bundle.putString("actionName", actionName);//share facebook
        bundle.putString("value", value);
        bundle.putString("actionCause", actionCause);

        bundle.putString("appVersion", BuildConfig.VERSION_NAME);
        bundle.putString("deviceModel", android.os.Build.MODEL);
        bundle.putString("deviceModel", android.os.Build.MODEL);
        bundle.putString("deviceOsVersion", android.os.Build.VERSION.RELEASE);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    public static void subscribeToFirebaseTopicIfNeeded(String topic) {

        if (!SettingManager.isSubscribedToFirebaseTopic(topic)) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
                SettingManager.setSubscribedToFirebaseTopic(topic, task.isSuccessful());
            });
        }
    }

    public static void unsubscribeToFirebaseTopic(String topic) {

        if (!SettingManager.isSubscribedToFirebaseTopic(topic)) {

            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SettingManager.setSubscribedToFirebaseTopic(topic, false);
                }
            });
        }
    }

    public static void saveFirebaseTokenToPreferenceIfNeeded() {
        if (StringUtil.isNullOrEmpty(SettingManager.getFirebaseToken())) {

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener((Task<String> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("FCM::::", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                SettingManager.setFirebaseToken(task.getResult());

                // Log and toast
                Log.e("FCM", task.getResult());
            });
        } else {
            Log.e("FCM:::: ", "From settings....." + SettingManager.getFirebaseToken());

        }
    }


    public static void saveFirebaseInstallationIdToPreferenceIfNeeded() {
        if (StringUtil.isNullOrEmpty(SettingManager.getFirebaseInstallationId())) {
            FirebaseInstallations.getInstance().getId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SettingManager.setFirebaseInstallationId(task.getResult());
                } else {
                    Log.e("Installations", "Unable to get Installation ID");
                }
            });
        } else {
            Log.e("Installations", "Setting  FirebaseInstallationId() ID: " + SettingManager.getFirebaseInstallationId());

        }
    }

    public static void unsubscribeFromFirebaseTopic(String topic) {
        if (SettingManager.isSubscribedToFirebaseTopic(topic)) {

            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SettingManager.setSubscribedToFirebaseTopic(topic, false);
                }
            });
        }
    }


    public static void initRemoteConfig(FirebaseRemoteConfig mFirebaseRemoteConfig, Context context) {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 60 : 6 * 3600)
                .build();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean updated = task.getResult();
            }
        });
    }
}
