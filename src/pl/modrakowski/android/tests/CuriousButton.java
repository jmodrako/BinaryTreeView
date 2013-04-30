package pl.modrakowski.android.tests;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import pl.modrakowski.android.Browse;

/**
 * User: Jack Modrakowski
 * Date: 4/23/13
 * Time: 10:40 AM
 * Departament: IT Mobile
 * Company: Implix
 */
public class CuriousButton extends Button {
    private static final String TAG = "curious_button";

    public CuriousButton(Context context) {
        super(context);
    }

    public CuriousButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CuriousButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Browse.Logger.i(TAG + ", dispatchTouchEvent: " + event.getActionMasked() + ", time: " + event.getEventTime());
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Browse.Logger.i(TAG + ", onTouchEvent: " + event.getActionMasked() + ", time: " + event.getEventTime());
        return super.onTouchEvent(event);
    }
}
