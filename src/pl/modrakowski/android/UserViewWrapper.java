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

/**
 * User: Jack Modrakowski
 * Date: 4/19/13
 * Time: 1:47 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class UserViewWrapper extends FrameLayout {

    public static enum WrapperUserType {
        CHILD, PARENT
    }

    public static enum WrapperState {
        IDLE, ANIMATING
    }

    public static enum OpenDirection {
        TO_LEFT, TO_RIGHT, NONE
    }

    private long animationDuration;
    private long openThresholdPx;
    private long closeThresholdPx;

    private boolean isForegroundViewIsOpen;
    private float downX;
    private float curentTransX;

    private UserBackgroundView userBackgroundView;
    private UserForegroundView userForegroundView;

    private WrapperUserType wrapperUserType;
    private WrapperState wrapperState;
    private static OpenDirection openDirection;

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
        if (wrapperState.equals(WrapperState.IDLE)) {
            if (isForegroundViewIsOpen) {
                handleTouchOnOpenForegroundView(event);
            } else {
                handleTouchOnCloseForegroundView(event);
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

    public static void setOpenDirection(OpenDirection direction) {
        openDirection = direction;
    }


    private void setForegroundViewIsOpen(boolean foregroundViewIsOpen) {
        isForegroundViewIsOpen = foregroundViewIsOpen;
        userBackgroundView.setEnableViews(foregroundViewIsOpen);
    }

    private void handleTouchOnOpenForegroundView(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = motionEvent.getRawX();
                curentTransX = ViewHelper.getTranslationX(userForegroundView);
                break;
            case MotionEvent.ACTION_MOVE:
                float dxWithCurrentTransX = motionEvent.getRawX() - downX + curentTransX;

                if (openDirection.equals(OpenDirection.TO_LEFT) && Math.signum(motionEvent.getRawX() - downX) == 1) {
                    // Show partially background view.
                    float leftEdge = userBackgroundView.getRight() - Math.abs(dxWithCurrentTransX);
                    float rightEdge = userBackgroundView.getRight();
                    userBackgroundView.setVisibleWidth(leftEdge, rightEdge);

                    // Apply translation x of foreground view.
                    ViewHelper.setTranslationX(userForegroundView, dxWithCurrentTransX);
                } else if (openDirection.equals(OpenDirection.TO_RIGHT) && Math.signum(motionEvent.getRawX() - downX) == -1) {
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
                        if (motionEvent.getRawX() - downX < closeThresholdPx) {
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
                        if (motionEvent.getRawX() - downX < -closeThresholdPx) {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(0, (Float) valueAnimator.getAnimatedValue());
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setWrapperState(WrapperState.IDLE);
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
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setWrapperState(WrapperState.IDLE);
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
            case MotionEvent.ACTION_DOWN:
                downX = motionEvent.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getRawX() - downX;

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
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setWrapperState(WrapperState.IDLE);
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
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setWrapperState(WrapperState.IDLE);
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
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(true);
                                    setWrapperState(WrapperState.IDLE);
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
                                    setWrapperState(WrapperState.ANIMATING);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    setForegroundViewIsOpen(false);
                                    setWrapperState(WrapperState.IDLE);
                                }
                            };
                            cancelAnimation(userForegroundView, updateListener, animatorListener, 0);
                        }
                        break;
                }
                break;
        }
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


    private void setWrapperState(WrapperState wrapperState) {
        this.wrapperState = wrapperState;
    }


    private void init(Context context) {
        setWillNotDraw(false);

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

        // Set initial state of wrapper.
        setWrapperState(WrapperState.IDLE);

        // Set initial direction opening views.
        setOpenDirection(OpenDirection.TO_RIGHT);

        // Set preDrawListener to change colors on views.
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                switch (getId()) {
                    case R.id.parent:
                        userForegroundView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                        wrapperUserType = WrapperUserType.PARENT;
                        break;
                    case R.id.left_child:
                    case R.id.right_child:
                        wrapperUserType = WrapperUserType.CHILD;
                        userForegroundView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                        break;
                }
                return true;
            }
        });

        // Initialize constants from resources.
        animationDuration = getResources().getInteger(R.integer.swipe_in_out_duration_ms);
        openThresholdPx = getResources().getInteger(R.integer.open_threshold_px);
        closeThresholdPx = getResources().getInteger(R.integer.close_threshold_px);

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

}
