package net.henriquedantas.android.touchanddraw;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by hdantas on 12/11/14.
 * Custom RadioGroup that aggregates ColorfulButtons
 */
public class ColorfulRadioGroup extends RadioGroup {

    public ColorfulRadioGroup(Context context) {
        super(context);
    }

    public ColorfulRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setButtonsColor(int buttonsColor) {
        for (int j = 0; j < getChildCount(); j++) {
            View child = getChildAt(j);
            if (child instanceof ColorfulRadioButton) {
                ((ColorfulRadioButton) child).setButtonColor(buttonsColor);
            }
        }
    }

}
