package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by IINEWMANII on 11/16/2016.
 * Class used to make textviews look like an old school lcd screen.
 */

public class ClockTextView extends android.support.v7.widget.AppCompatTextView {

    public ClockTextView(Context context) {

        super(context);

        applyCustomFont(context);
    }

    public ClockTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        applyCustomFont(context);

    }

    public ClockTextView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        applyCustomFont(context);

    }

    private void applyCustomFont(Context context) {

        try {
            Typeface clockFace = FontCache.get(context);
            setTypeface(clockFace);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
