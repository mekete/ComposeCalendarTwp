package com.shalom.calendar.utility;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

public class LocaleUtil {

    public static String getMessage(String key, String propertyFile, Locale locale, String defaultMessage) {
        return key;
    }


    public static void setDefaultLocale(Context context, String localeLanguage) {
        Locale locale = new Locale(localeLanguage);
        Configuration config = context.getResources().getConfiguration();
        setDefaultLocale(context, config, locale);

    }

    public static void setDefaultLocale(Context context, Configuration config , Locale locale) {
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        context.getResources().updateConfiguration(config, null);
        Log.e("TAG", "restartAppWithNewLanguageConfiguration: \n\nupdateConfiguration\ncalledconfig");


    }
}
