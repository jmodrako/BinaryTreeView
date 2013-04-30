package pl.modrakowski.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * User: Jack Modrakowski
 * Date: 4/19/13
 * Time: 1:47 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class UserBackgroundView extends FrameLayout {

    private RectF clipPath;

    private int viewWidth;
    private int viewHeight;
    private boolean enableViews;

    public UserBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public UserBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipRect(clipPath, Region.Op.INTERSECT);
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWidth = w;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !enableViews;
    }

    public void setVisibleWidth(float width) {
        if (width > 0) {
            clipPath.set(viewWidth, getTop(), viewWidth, getBottom());
            invalidate(viewWidth, getTop(), viewWidth, getBottom());
        } else {
            clipPath.set(viewWidth + width, getTop(), viewWidth, getBottom());
            invalidate((int) (viewWidth + width), getTop(), viewWidth, getBottom());
        }
    }

    public void setVisibleWidth(float left, float right) {
        clipPath.set(left, getTop(), right, getBottom());
        invalidate((int) left, getTop(), (int) right, getBottom());
    }

    public void setEnableViews(boolean enable) {
        enableViews = enable;
    }

    private void init(Context context) {
        setWillNotDraw(false);
        clipPath = new RectF(viewWidth, 0, 0, viewHeight);
    }
}
