package pl.modrakowski.android;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * User: Jack Modrakowski
 * Date: 5/25/13
 * Time: 7:22 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class CircularImageView extends ImageView {

    private Bitmap picture;
    private Paint paint = new Paint();
    private Paint darkBackgroundPaint;
    private Paint darkBackgroundPaint2;
    private Paint dashedLinePaint;

    private BitmapShader shader;

    public CircularImageView(Context context) {
        super(context);
        init(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        darkBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //darkBackgroundPaint.setColor(context.getResources().getColor(R.color.light_gray));
        darkBackgroundPaint.setColor(Color.parseColor("#E0E0E0"));
        darkBackgroundPaint.setStyle(Paint.Style.FILL);

        dashedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashedLinePaint.setStyle(Paint.Style.STROKE);
        dashedLinePaint.setColor(context.getResources().getColor(R.color.light_gray_2));
        dashedLinePaint.setStrokeWidth(1.7f);
        dashedLinePaint.setPathEffect(new DashPathEffect(new float[]{12, 3}, 0));

        darkBackgroundPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkBackgroundPaint2.setColor(context.getResources().getColor(R.color.light_gray_3));
        darkBackgroundPaint2.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        shader = new BitmapShader(((BitmapDrawable) getDrawable()).getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setAntiAlias(true);
        paint.setShader(shader);
        int circlePoint = getWidth() / 2;
        canvas.drawCircle(circlePoint, circlePoint, circlePoint - 5, darkBackgroundPaint2);
        canvas.drawCircle(circlePoint, circlePoint, circlePoint - 8, darkBackgroundPaint);
        canvas.drawCircle(circlePoint, circlePoint, circlePoint - 13, dashedLinePaint);
        canvas.drawCircle(circlePoint, circlePoint, circlePoint - 18, paint);
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
        invalidate();
    }
}
