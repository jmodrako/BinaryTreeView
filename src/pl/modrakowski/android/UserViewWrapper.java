package pl.modrakowski.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import com.nineoldandroids.animation.*;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;

/**
 * User: Jack Modrakowski
 * Date: 4/19/13
 * Time: 1:47 PM
 * Departament: IT Mobile
 * Company: Implix
 * <p/>
 * This view is responsible for maintenance two other views: background view (@UserBackgroundView) and foreground view (@UserForegroundView).
 */
public class UserViewWrapper extends FrameLayout {

    public static enum WrapperUserType {
        LEFT_CHILD, RIGHT_CHILD, PARENT
    }

    public static enum WrapperState {
        IDLE, ANIMATING, VERTICAL_MOVE, HORIZONTAL_MOVE
    }

    public static enum OpenDirection {
        TO_LEFT, TO_RIGHT, NONE
    }

    private UserBackgroundView userBackgroundView;
    private UserForegroundView userForegroundView;

    private WrapperUserType wrapperUserType;
    private WrapperState currentWrapperState;
    private WrapperState previousWrapperState;
    private OpenDirection openDirection;

    private static ArrayList<UserViewWrapper> sWrappers = new ArrayList<UserViewWrapper>();

    private long animationDuration;
    private long openThresholdPx;
    private long closeThresholdPx;
    private long scaledTouchSlope;

    private boolean isForegroundViewIsOpen;

    private int mDownX;
    private int mDownY;
    private int mOriginalViewTop;

    private float mCurentTransX;


    public UserViewWrapper(Context context) {
        super(context);
        init(context);
    }

    public UserViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // We can handle touch events only when state is not ANIMATING.
        if (!currentWrapperState.equals(WrapperState.ANIMATING)) {

            switch (currentWrapperState) {

                case IDLE:
                    setCurrentWrapperState(determineWrapperState(event));
//                    Browse.Logger.i("determineWrapperState: " + currentWrapperState);

                    // Watch for wrapper state changes.
                    if (previousWrapperState.equals(WrapperState.IDLE) && currentWrapperState.equals(WrapperState.VERTICAL_MOVE)) {
                        sendMsgToRestOfWrappers(CallbackMsg.SET_INVISIBLE);
                    }
                    break;
                case VERTICAL_MOVE:

                    handleTouchUpDown(event);

                    break;
                case HORIZONTAL_MOVE:

                    if (isForegroundViewIsOpen) {
                        handleTouchOnOpenForegroundView(event);
                    } else {
                        handleTouchOnCloseForegroundView(event);
                    }

                    break;
            }

            return true;
        } else {
            return false;
        }
    }


    public void startPromptAnimation(boolean immediately, long delay) {
        final AnimatorSet animatorSet = new AnimatorSet();
        final ObjectAnimator initOpen = ObjectAnimator.ofFloat(userForegroundView, "translationX", 200);
        initOpen.setInterpolator(new AccelerateDecelerateInterpolator());
        initOpen.setDuration(500);
        initOpen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                userBackgroundView.setVisibleWidth(0, (Float) valueAnimator.getAnimatedValue());
            }
        });


        final ObjectAnimator closeWithBounce = ObjectAnimator.ofFloat(userForegroundView, "translationX", 0);
        closeWithBounce.setInterpolator(new BounceInterpolator());
        closeWithBounce.setDuration(800);
        closeWithBounce.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                userBackgroundView.setVisibleWidth(0, (Float) valueAnimator.getAnimatedValue());
            }
        });

        animatorSet.playSequentially(initOpen, closeWithBounce);

        if (immediately) {
            post(new Runnable() {
                @Override
                public void run() {
                    animatorSet.start();
                }
            });
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    animatorSet.start();
                }
            }, delay);
        }
    }

    public void setOpenDirection(OpenDirection direction) {
        openDirection = direction;
    }


    private void setForegroundViewIsOpen(boolean foregroundViewIsOpen) {
        isForegroundViewIsOpen = foregroundViewIsOpen;
        userBackgroundView.setEnableViews(foregroundViewIsOpen);
    }


    private void callbackFromOtherWrapperViews(int callbackMsg) {
        Browse.Logger.i("Current view: " + wrapperUserType + ", callbackMsg: " + callbackMsg);

        switch (callbackMsg) {
            case CallbackMsg.SET_INVISIBLE:
                setVisibility(View.INVISIBLE);
                postInvalidate();
                break;
            case CallbackMsg.SET_VISIBLE:
                setVisibility(View.VISIBLE);
                postInvalidate();
                break;
        }

    }

    private void sendMsgToRestOfWrappers(int callbackMsg) {
        for (UserViewWrapper userViewWrapper : sWrappers) {
            if (!userViewWrapper.wrapperUserType.equals(wrapperUserType)) {
                userViewWrapper.callbackFromOtherWrapperViews(callbackMsg);
            }
        }
    }

    private void handleTouchOnOpenForegroundView(MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()) {

            case MotionEvent.ACTION_MOVE:
                float dxWithCurrentTransX = motionEvent.getRawX() - mDownX + mCurentTransX;

                if (openDirection.equals(OpenDirection.TO_LEFT) && Math.signum(motionEvent.getRawX() - mDownX) == 1) {
                    // Show partially background view.
                    float leftEdge = userBackgroundView.getRight() - Math.abs(dxWithCurrentTransX);
                    float rightEdge = userBackgroundView.getRight();
                    userBackgroundView.setVisibleWidth(leftEdge, rightEdge);

                    // Apply translation x of foreground view.
                    ViewHelper.setTranslationX(userForegroundView, dxWithCurrentTransX);
                } else if (openDirection.equals(OpenDirection.TO_RIGHT) && Math.signum(motionEvent.getRawX() - mDownX) == -1) {
                    // Show partially background view.
                    float leftEdge = userBackgroundView.getLeft();
                    float rightEdge = userBackgroundView.getLeft() + Math.abs(dxWithCurrentTransX);
                    userBackgroundView.setVisibleWidth(leftEdge, rightEdge);

                    // Apply translation x of foreground view.
                    ViewHelper.setTranslationX(userForegroundView, dxWithCurrentTransX);
                }

                break;
            case MotionEvent.ACTION_UP:
                switch (openDirection) {
                    case TO_LEFT:
                        if (motionEvent.getRawX() - mDownX < closeThresholdPx) {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth((Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                }
                            };
                            cancelAnimation(userForegroundView, updateListener, animatorListener, -userForegroundView.getMeasuredWidth());
                        } else {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth((Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                }
                            };
                            swipeAnimation(userForegroundView, updateListener, animatorListener, 0);
                        }
                        break;
                    case TO_RIGHT:
                        if (motionEvent.getRawX() - mDownX < -closeThresholdPx) {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(0, (Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }
                            };
                            swipeAnimation(userForegroundView, updateListener, animatorListener, 0);
                        } else {
                            // Back to fully visible background view.
                            // Hide foreground view.
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(0, (Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }
                            };
                            cancelAnimation(userForegroundView, updateListener, animatorListener, userForegroundView.getMeasuredWidth()
                            );
                        }
                        break;
                }
                break;
        }
    }

    private void handleTouchOnCloseForegroundView(MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()) {

            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getRawX() - mDownX;

                if (openDirection.equals(OpenDirection.TO_LEFT) && Math.signum(dx) == -1) {
                    // Show partially background view.
                    float leftEdge = userBackgroundView.getRight() - Math.abs(dx);
                    float rightEdge = userBackgroundView.getRight();
                    userBackgroundView.setVisibleWidth(leftEdge, rightEdge);

                    // Apply translation x of foreground view.
                    ViewHelper.setTranslationX(userForegroundView, dx);
                } else if (openDirection.equals(OpenDirection.TO_RIGHT) && Math.signum(dx) == 1) {
                    // Show partially background view.
                    float leftEdge = userBackgroundView.getLeft();
                    float rightEdge = userBackgroundView.getLeft() + Math.abs(dx);
                    userBackgroundView.setVisibleWidth(leftEdge, rightEdge);

                    // Apply translation x of foreground view.
                    ViewHelper.setTranslationX(userForegroundView, dx);
                }

                break;
            case MotionEvent.ACTION_UP:
                switch (openDirection) {
                    case TO_LEFT:
                        if (ViewHelper.getTranslationX(userForegroundView) < -openThresholdPx) {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth((Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }
                            };
                            swipeAnimation(userForegroundView, updateListener, animatorListener, -userForegroundView.getMeasuredWidth());
                        } else {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth((Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }
                            };
                            cancelAnimation(userForegroundView, updateListener, animatorListener, 0);
                        }
                        break;
                    case TO_RIGHT:
                        if (ViewHelper.getTranslationX(userForegroundView) > openThresholdPx) {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(userBackgroundView.getLeft(), userBackgroundView.getLeft() + (Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }

                            };
                            swipeAnimation(userForegroundView, updateListener, animatorListener, userForegroundView.getMeasuredWidth());
                        } else {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(userBackgroundView.getLeft(), Math.abs((Float) valueAnimator.getAnimatedValue()));
                                    //userBackgroundView.setVisibleWidth((Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setCurrentWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setCurrentWrapperState(WrapperState.IDLE);
                                }
                            };
                            cancelAnimation(userForegroundView, updateListener, animatorListener, 0);
                        }
                        break;
                }
                break;
        }
    }

    private void handleTouchUpDown(MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()) {

            case MotionEvent.ACTION_MOVE:

                float dy = motionEvent.getRawY() - mDownY;
                ViewHelper.setTranslationY(this, dy);

                break;

            case MotionEvent.ACTION_UP:
                moveViewBackToOriginalPlace(this, new BetterAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animator) {

                        setCurrentWrapperState(WrapperState.IDLE);
                        sendMsgToRestOfWrappers(CallbackMsg.SET_VISIBLE);

                    }
                }, mOriginalViewTop - getTop());

                break;
        }

    }


    private WrapperState determineWrapperState(MotionEvent motionEvent) {

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // mDown's variables and mCurrentTransx is use to do calculations below and in methods handle(Open/Close)ForegroundView.
                mDownX = (int) motionEvent.getRawX();
                mDownY = (int) motionEvent.getRawY();
                mCurentTransX = ViewHelper.getTranslationX(userForegroundView);
                mOriginalViewTop = getTop();

                break;

            case MotionEvent.ACTION_MOVE:
                int currentX = (int) motionEvent.getRawX();
                int currentY = (int) motionEvent.getRawY();
                int dx = Math.abs(currentX - mDownX);
                int dy = Math.abs(currentY - mDownY);

                /*Browse.Logger.i("X: " + dx);
                Browse.Logger.i("Y: " + dy);*/

                if (dx > scaledTouchSlope && !currentWrapperState.equals(WrapperState.VERTICAL_MOVE)) {
                    return WrapperState.HORIZONTAL_MOVE;
                } else if (dy > scaledTouchSlope && !currentWrapperState.equals(WrapperState.HORIZONTAL_MOVE)) {
                    return WrapperState.VERTICAL_MOVE;
                } else {
                    return WrapperState.IDLE;
                }
        }

        return currentWrapperState;
    }

    private void swipeAnimation(final View swipeView, ValueAnimator.AnimatorUpdateListener animatorListener, Animator.AnimatorListener listener, float translation) {
        ObjectAnimator applyTranslationX = ObjectAnimator.ofFloat(swipeView, "translationX", translation);
        applyTranslationX.setDuration(animationDuration);
        applyTranslationX.setEvaluator(new FloatEvaluator());
        applyTranslationX.addUpdateListener(animatorListener);
        applyTranslationX.addListener(listener);
        applyTranslationX.start();
    }

    private void cancelAnimation(final View swipeView, ValueAnimator.AnimatorUpdateListener animatorListener, Animator.AnimatorListener listener, float translation) {
        ObjectAnimator cancelTranslationX = ObjectAnimator.ofFloat(swipeView, "translationX", translation);
        cancelTranslationX.setDuration(animationDuration);
        cancelTranslationX.addUpdateListener(animatorListener);
        cancelTranslationX.setEvaluator(new FloatEvaluator());
        cancelTranslationX.addListener(listener);
        cancelTranslationX.start();
    }

    private void moveViewBackToOriginalPlace(final View view, Animator.AnimatorListener listener, float translation) {
        ObjectAnimator cancelTranslationX = ObjectAnimator.ofFloat(view, "translationY", translation);
        cancelTranslationX.setDuration(animationDuration);
        //cancelTranslationX.addUpdateListener(animatorListener);
        //cancelTranslationX.setEvaluator(new FloatEvaluator());
        cancelTranslationX.addListener(listener);
        cancelTranslationX.start();
    }


    private void setCurrentWrapperState(WrapperState wrapperState) {
        if (currentWrapperState == null) {
            throw new RuntimeException("Current wrapper state must be set!");
        }

        setPreviousWrapperState(currentWrapperState);
        currentWrapperState = wrapperState;
    }

    private void setPreviousWrapperState(WrapperState wrapperState) {
        previousWrapperState = wrapperState;
    }


    private void init(Context context) {
        setWillNotDraw(false);

        // Set previous state of wrapper.
        setPreviousWrapperState(WrapperState.IDLE);

        // Set initial state of wrapper.
        setCurrentWrapperState(WrapperState.IDLE);

        // Set initial direction opening views.
        setOpenDirection(OpenDirection.TO_RIGHT);

        // Initialize foreground/background views.
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child.getTag() != null && ((String) child.getTag()).equalsIgnoreCase(getResources().getString(R.string.foreground_view_tag))) {
                    userForegroundView = (UserForegroundView) child;
                    userForegroundView.setVisibility(View.VISIBLE);

                    //startPromptAnimation(false, 2500);

                } else if (child.getTag() != null && ((String) child.getTag()).equalsIgnoreCase(getResources().getString(R.string.background_view_tag))) {
                    userBackgroundView = (UserBackgroundView) child;
                }
            }

            @Override
            public void onChildViewRemoved(View view, View view2) {

            }
        });

        // Set preDrawListener to change colors on views.
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                switch (getId()) {
                    case R.id.parent:
                        wrapperUserType = WrapperUserType.PARENT;
                        break;
                    case R.id.left_child:
                        wrapperUserType = WrapperUserType.LEFT_CHILD;
                        break;
                    case R.id.right_child:
                        wrapperUserType = WrapperUserType.RIGHT_CHILD;
                        break;
                }
                return true;
            }
        });

        // Initialize constants from resources.
        animationDuration = getResources().getInteger(R.integer.swipe_in_out_duration_ms);
        openThresholdPx = getResources().getInteger(R.integer.open_threshold_px);
        closeThresholdPx = getResources().getInteger(R.integer.close_threshold_px);
        scaledTouchSlope = getResources().getInteger(R.integer.scaled_touch_slope);

        // Register itself as listener for messages from other wrappers.
        sWrappers.add(this);
    }


    private static class BetterAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    private static class CallbackMsg {
        public static final int SET_INVISIBLE = 1 << 1;
        public static final int SET_VISIBLE = 1 << 2;
    }
}
