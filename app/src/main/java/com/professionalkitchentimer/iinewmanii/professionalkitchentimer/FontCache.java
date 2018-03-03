package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IINEWMANII on 11/16/2016.
 * Class used to cache font to speed up app on slower devices and use less memory.
 */

class FontCache {

    private static final Map<String, Typeface> fontCache = new HashMap<>();

    private FontCache() {

    }

    static Typeface get(Context context) {

        Typeface typeface = fontCache.get("fonts/alarmclock.ttf");

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/alarmclock.ttf");
            } catch (Exception e) {
                return null;
            }

            fontCache.put("fonts/alarmclock.ttf", typeface);
        }

        return typeface;
    }
}
