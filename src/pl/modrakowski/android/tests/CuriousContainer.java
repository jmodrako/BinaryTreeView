package pl.modrakowski.android.tests;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import pl.modrakowski.android.Browse;

/**
 * User: Jack Modrakowski
 * Date: 4/23/13
 * Time: 10:38 AM
 * Departament: IT Mobile
 * Company: Implix
 */
public class CuriousContainer extends FrameLayout {

    private String TAG = "curious_container_";

    public CuriousContainer(Context context) {
        super(context);
    }

    public CuriousContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CuriousContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Browse.Logger.i(TAG + ", onInterceptTouchEvent: " + event.getActionMasked() + ", time: " + event.getEventTime());
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Browse.Logger.i(TAG + ", onTouchEvent: " + event.getActionMasked() + ", time: " + event.getEventTime());
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Browse.Logger.i(TAG + ", dispatchTouchEvent: " + event.getActionMasked() + ", time: " + event.getEventTime());
        return super.dispatchTouchEvent(event);
    }

    public void setTAG(String tag) {
        TAG += tag;
    }
}
