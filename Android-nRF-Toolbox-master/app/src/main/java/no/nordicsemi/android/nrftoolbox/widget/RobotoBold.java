package no.nordicsemi.android.nrftoolbox.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import no.nordicsemi.android.nrftoolbox.R;

public class RobotoBold extends AppCompatTextView {
    public RobotoBold(Context context) {
        super(context);

        init();
    }

    public RobotoBold(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public RobotoBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        if (!isInEditMode()) {
            final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
            setTypeface(typeface);
        }
    }
}
