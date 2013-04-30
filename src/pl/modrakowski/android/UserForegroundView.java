package pl.modrakowski.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * User: Jack Modrakowski
 * Date: 4/19/13
 * Time: 1:47 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class UserForegroundView extends FrameLayout {

    private boolean isVisible;

    public UserForegroundView(Context context) {
        super(context);
        init(context);
    }

    public UserForegroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserForegroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;

        if (visible) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(INVISIBLE);
        }
    }

    private void init(Context context) {
        setWillNotDraw(false);
    }
}
