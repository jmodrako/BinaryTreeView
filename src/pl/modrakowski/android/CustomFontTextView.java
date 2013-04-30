package pl.modrakowski.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class CustomFontTextView extends TextView {

    private static final String TAG = CustomFontTextView.class.getSimpleName();
    private static final float MIN_TEXT_SIZE = 1.0f;
    private static final Canvas TEXT_RESIZE_CANVAS = new Canvas();

    private boolean autoSize = false;
    private boolean mNeedsResize = true;
    private float mSpacingMult = 1.0f;
    private float mSpacingAdd = 0.0f;
    private float defaultSize;

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttribiute(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttribiute(context, attrs);
    }

    private void parseAttribiute(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        String customFont = a.getString(R.styleable.CustomFontTextView_customFont);
        autoSize = a.getBoolean(R.styleable.CustomFontTextView_autoSize, false);
        setSingleLine(autoSize);
        if (customFont != null && !customFont.equals("")) {
            setCustomFont(ctx, customFont);
        }
        defaultSize = getTextSize();
        a.recycle();
        // Improve custom font apperance.
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public boolean setCustomFont(Context ctx, String asset) {
        try {
            Typeface tf = TypefacesUtils.get(asset, getContext());
            setTypeface(tf);
        } catch (Exception e) {
            Log.e(TAG, "Could not get typeface: " + e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        if (autoSize) {
            mNeedsResize = true;
            requestLayout();
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if ((changed || mNeedsResize) && autoSize) {
            resizeText(getWidth() - getTotalPaddingLeft() - getTotalPaddingRight(), getHeight() - getTotalPaddingTop() - getTotalPaddingBottom());
        }

    }

    private void resizeText(int widthPx, int heightPx) {
        CharSequence text = getText();
        // Do not resize if the view does not have dimensions or there is no text
        if (text == null || text.length() == 0 || widthPx <= 0) {
            return;
        }

        // Get the text view's paint object (as a copy, so we don't modify it)
        TextPaint textPaint = new TextPaint();
        textPaint.set(getPaint());

        /*Logger.i("TextPaint");
        textPaint.getTextBounds((String) text, 0, text.length(), textBounds);
        Logger.i("Text bounds rect: " + textBounds);
        Logger.i("Text bounds rect height: " + textBounds.height());
        Logger.i("Text view height: " + getMeasuredHeight());
        Logger.i("Text text: " + text);
        Logger.i("Text size: " + textPaint.getTextSize());*/

        // If there is a max text size set, use that; otherwise, base the max text size
        // on the current text size.
        float targetTextSize = defaultSize;


        int lineCount = getTextLineCount(text, textPaint, widthPx, targetTextSize);
        while (lineCount > 1 && targetTextSize > MIN_TEXT_SIZE) {
            targetTextSize -= 1;
            lineCount = getTextLineCount(text, textPaint, widthPx, targetTextSize);
        }


        int height = getTextHeight(text, textPaint, widthPx, targetTextSize);
        while (height > heightPx && targetTextSize > MIN_TEXT_SIZE) {
            targetTextSize -= 1;
            height = getTextHeight(text, textPaint, widthPx, targetTextSize);
        }

        // Some devices try to auto adjust line spacing, so force default line spacing
        // and invalidate the layout as a side effect
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
        setLineSpacing(mSpacingAdd, mSpacingMult);
        // Reset force resize flag
        mNeedsResize = false;
    }

    private int getTextLineCount(CharSequence source, TextPaint paint, int widthPx, float textSize) {
        // Update the text paint object
        paint.setTextSize(textSize);

        // Draw using a static layout
        StaticLayout layout = new StaticLayout(source, paint, widthPx, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                mSpacingAdd, true);

        try {
            layout.draw(TEXT_RESIZE_CANVAS);
        } catch (Exception e) {
            return 1;
        }
        return layout.getLineCount();

    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        // Update the text paint object
        paint.setTextSize(textSize);

        // Draw using a static layout
        StaticLayout layout = new StaticLayout(source, paint, width, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                mSpacingAdd, true);
        try {
            layout.draw(TEXT_RESIZE_CANVAS);
        } catch (Exception e) {
            return (int) textSize;
        }
        return layout.getHeight();
    }
}
