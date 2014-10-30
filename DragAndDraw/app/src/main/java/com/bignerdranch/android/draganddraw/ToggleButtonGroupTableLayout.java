package com.bignerdranch.android.draganddraw;

/**
 * Created by nuno on 29/10/14.
 */

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
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
                    Log.d(TAG, "isChecked: " + getResources().getResourceEntryName(rb.getId()));
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

        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, activeRadioButton.getId());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        int activeRadioButtonId = savedState.getActiveRadioButtonId();

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

    }

    /**
     * The dispatchSaveInstanceState() and dispatchRestoreInstanceState() methods are overridden
     * to ensure the onSaveInstance() and onRestoreInstance() are not called on the child views.
     * src: http://charlesharley.com/2012/programming/views-saving-instance-state-in-android/
     */
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray container) {
        super.dispatchThawSelfOnly(container);
    }

    /**
     * Convenience class to save / restore the state.
     * adapted from: https://github.com/CharlesHarley/Example-Android-SavingInstanceState/blob/master/src/com/example/android/savinginstancestate/views/LockCombinationPicker.java
     */
    protected static class SavedState extends BaseSavedState {

        private final int activeRadioButtonId;


        private SavedState(Parcelable superState, int id) {
            super(superState);
            activeRadioButtonId = id;
        }

        private SavedState(Parcel in) {
            super(in);
            activeRadioButtonId = in.readInt();
        }

        public int getActiveRadioButtonId() {
            return activeRadioButtonId;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(activeRadioButtonId);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}