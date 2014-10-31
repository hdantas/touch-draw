package com.bignerdranch.android.draganddraw;

/**
 * Created by nuno on 29/10/14.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 *
 */
public class ToggleButtonGroupTableLayout extends TableLayout implements OnClickListener {
    private static final String TAG = ToggleButtonGroupTableLayout.class.getSimpleName();

    private static final String EXTRA_CURRENT_RADIO_BUTTON_ID = "extra_current_radio_button_id";
    private static final String EXTRA_STATE = "extra_state";

    private RadioButton activeRadioButton;

    public ToggleButtonGroupTableLayout(Context context) {
        super(context);
        Log.d(TAG, "constructor w/o attrs");
    }

    public ToggleButtonGroupTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "constructor w/ attrs");
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if (activeRadioButton != null) {
            Log.d(TAG, "onClick CLEAR " + getResources().getResourceEntryName(activeRadioButton.getId()));
            activeRadioButton.setChecked(false);
        }
        Log.d(TAG, "onClick CHECK " + getResources().getResourceEntryName(rb.getId()));
        rb.setChecked(true);
        activeRadioButton = rb;
        super.performClick();

    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        isChecked((TableRow) child);
        setChildrenOnClickListener((TableRow) child);
    }


    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        isChecked((TableRow) child);
        setChildrenOnClickListener((TableRow) child);
    }


    private void isChecked(TableRow tr) {
        final int c = tr.getChildCount();
        for (int i = 0; i < c; i++) {
            final View v = tr.getChildAt(i);
            if (v instanceof RadioButton) {
                RadioButton rb = (RadioButton) v;
                if (rb.isChecked()) {
//                    Log.d(TAG, "isChecked: " + this.getResources().getResourceEntryName(rb.getId()));
                    activeRadioButton = rb;
                }
            }
        }
    }

    private void setChildrenOnClickListener(TableRow tr) {
        final int c = tr.getChildCount();
        for (int i = 0; i < c; i++) {
            final View v = tr.getChildAt(i);
            if (v instanceof RadioButton) {
                v.setOnClickListener(this);
            }
        }
    }

    public int getCheckedRadioButtonId() {
        if (activeRadioButton != null) {
            return activeRadioButton.getId();
        }
        return -1;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState " + getResources().getResourceEntryName(activeRadioButton.getId()));

        Parcelable savedState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_STATE, savedState);
        bundle.putInt(EXTRA_CURRENT_RADIO_BUTTON_ID, activeRadioButton.getId());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Parcelable restoredState = ((Bundle) state).getParcelable(EXTRA_STATE);
        int activeRadioButtonId = ((Bundle) state).getInt(EXTRA_CURRENT_RADIO_BUTTON_ID);

        for (int i = 0; i < this.getChildCount(); i++) { // for all table rows
            TableRow tr = (TableRow) getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) { // for all cells (ie radioButtons)
                RadioButton rb = (RadioButton) tr.getChildAt(j);
                if (rb.getId() == activeRadioButtonId) {
                    activeRadioButton = rb;
                    activeRadioButton.setChecked(true);
                } else {
                    rb.setChecked(false);
                }
            }
        }
        super.performClick(); // to inform the parent the selected button may have changed
        Log.d(TAG, "onRestoreInstanceState " + getResources().getResourceEntryName(activeRadioButton.getId()));

        super.onRestoreInstanceState(restoredState);
    }

}