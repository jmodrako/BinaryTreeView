package pl.modrakowski.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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

    /**
     * Listener used for notify user about go level up action.
     */
    public static interface OnLevelUpListener {
        public void onLevelUp(ViewGroup backgroundView, ViewGroup foregroundView);
    }

    /**
     * Listener used for notify user about go level down action.
     */
    public static interface OnLevelDownListener {
        public void onLevelDown(ViewGroup backgroundView, ViewGroup foregroundView);
    }

    /**
     * Interface used for notify user about achieve threshold,
     * during movement up.
     */
    public static interface OnGoUpThresholdAchievedListener {
        public void onThresholdAchieved(ViewGroup backgroundView, ViewGroup foregroundView);

        public void onThresholdCancel(ViewGroup backgroundView, ViewGroup foregroundView);
    }

    /**
     * Interface used for notify user about achieve threshold,
     * during movement down.
     */
    public static interface OnGoDownThresholdAchievedListener {
        public void onThresholdAchieved(ViewGroup backgroundView, ViewGroup foregroundView);

        public void onThresholdCancel(ViewGroup backgroundView, ViewGroup foregroundView);
    }

    /**
     * Interface used for notify user about progress,
     * during movement up. Value from 0 to 1.
     */
    public static interface OnMoveUpProgressListener {
        public void moveUpProgress(float progress);
    }

    /**
     * Interface used for notify user about progress,
     * during movement down. Value from 0 to 1.
     */
    public static interface OnMoveDownProgressListener {
        public void moveDownProgress(float progress);
    }

    /**
     * Interface used for set custom action, when user hold
     * view above up threshold, for some time. That time is
     * described by longUpHoldTimeMs variable.
     */
    public static interface OnHoldDuringMoveUpListener {
        public void onTick(long progress);

        public void onFinish();

        public void onCancel();
    }

    /**
     * Interface used for set custom action, when user hold
     * view above down threshold, for some time. That time is
     * described by longDownHoldTimeMs variable.
     */
    public static interface OnHoldDuringMoveDownListener {
        public void onTick(long progress);

        public void onFinish();

        public void onCancel();
    }

    /**
     * Interface used for notify user about situation when views are
     * above the top edge of the screen. In this situation,
     * all views are outiside of the screen.
     * In this state we can change data on views.
     */
    public static interface OnViewsAreAtTheTopOfTheScreenListener {
        public void onViewsAreAtTheTopOfTheScreen(WrapperUserType wrapperUserType);
    }

    /**
     * Interface used for notify user about situation when views are
     * below the bottom edge of the screen. In this situation,
     * all views are outiside of the screen.
     * In this state we can change data on views.
     */
    public static interface OnViewsAreAtTheBottomOfTheScreenListener {
        public void onViewsAreAtTheBottomOfTheScreen(WrapperUserType wrapperUserType);
    }


    /**
     * Enum describes current view type by user role in the view.
     */
    public static enum WrapperUserType {
        LEFT_CHILD, RIGHT_CHILD, PARENT
    }

    /**
     * Enum describes current state of tree view.
     */
    public static enum WrapperState {
        IDLE, ANIMATING, VERTICAL_MOVE, HORIZONTAL_MOVE
    }

    /**
     * Enum describes direction in which view can be opened through slide it.
     */
    public static enum OpenDirection {
        TO_LEFT, TO_RIGHT, NONE
    }

    /**
     * Enum describes direction in which view can be slided up and down.
     */
    public static enum MoveDirection {
        UP, DOWN, BOTH, NONE
    }

    // List of all wrappers, used to notify other wrappers in tree.
    private static ArrayList<UserViewWrapper> sWrappers = new ArrayList<UserViewWrapper>();

    // Listeners for some actions in the tree.
    private static OnLevelUpListener onLevelUpListenerClassListener;
    private static OnLevelDownListener onLevelDownListenerClassListener;
    private static OnGoUpThresholdAchievedListener onGoUpThresholdAchievedListener;
    private static OnGoDownThresholdAchievedListener onGoDownThresholdAchievedListener;
    private static OnHoldDuringMoveUpListener onHoldDuringMoveUpListener;
    private static OnHoldDuringMoveDownListener onHoldDuringMoveDownListener;
    private static OnViewsAreAtTheTopOfTheScreenListener onViewsAreAtTheTopOfTheScreenListener;
    private static OnViewsAreAtTheBottomOfTheScreenListener onViewsAreAtTheBottomOfTheScreenListener;

    // Wrapper for background view.
    private UserBackgroundView userBackgroundView;
    // Wrapper for foreground view.
    private UserForegroundView userForegroundView;

    private WrapperUserType wrapperUserType;
    private WrapperState currentWrapperState;
    private WrapperState previousWrapperState;

    private OpenDirection openDirection;
    private MoveDirection moveDirection;

    private OnLevelUpListener onLevelUpListener;
    private OnLevelDownListener onLevelDownListener;

    private OnMoveUpProgressListener onMoveUpProgressListener;
    private OnMoveDownProgressListener onMoveDownProgressListener;

    private CountDownTimer countDownTimerDuringUpMove;
    private CountDownTimer countDownTimerDuringDownMove;

    private long slideUpDownViewsMs;
    private long openCloseViewDurationMs;
    private long scaledTouchSlope;
    private long treeViewPaddingPx;
    private long openThresholdPx;
    private long closeThresholdPx;
    private long goUpThresholdPx;
    private long goDownThresholdPx;

    private int longUpHoldTimeMs;
    private int longDownHoldTimeMs;

    private boolean isForegroundViewIsOpen;
    private boolean isViewCanBeMovedOutsideScreen;

    // Flag is use to determine is threshold to go UP was achieved.
    private boolean isGoUpThresholdAchieved;
    // Flag is use to determine is threshold to go DOWN was achieved.
    private boolean isGoDownThresholdAchieved;

    private int mDownX;
    private int mDownY;
    private int mOriginalViewTop;
    private int mOriginalViewBottom;
    private int mInterceptEventX;

    private float mCurentTransX;


    public UserViewWrapper(Context context) {
        super(context);
        init(context, null);
    }

    public UserViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public UserViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Browse.Logger.i("Current state: " + currentWrapperState);

        // We can handle touch events only when state is not ANIMATING.
        if (!currentWrapperState.equals(WrapperState.ANIMATING)) {

            switch (currentWrapperState) {
                case IDLE:
                    setCurrentWrapperState(determineWrapperState(event));
                    //Browse.Logger.i("determineWrapperState: " + currentWrapperState);

                    // Watch for wrapper state changes.
                    // IDLE --> VERTICAL_MODE
                    if (previousWrapperState.equals(WrapperState.IDLE) && currentWrapperState.equals(WrapperState.VERTICAL_MOVE)) {
                        // Adjustment for jank during swiping.
                        // Couse because mDownY was translated by scaledTouchSlope.
                        if (moveDirection.equals(MoveDirection.DOWN)) {
                            mDownY += scaledTouchSlope;
                        } else if (moveDirection.equals(MoveDirection.UP)) {
                            mDownY -= scaledTouchSlope;
                        }
                        sendMsgToRestOfWrappers(CallbackMsg.SET_INVISIBLE, false);
                    } else if (previousWrapperState.equals(WrapperState.IDLE) && currentWrapperState.equals(WrapperState.HORIZONTAL_MOVE)) {
                        // Adjustment for jank during opening.
                        // Couse because mDownX was translated by scaledTouchSlope.
                        if (openDirection.equals(OpenDirection.TO_LEFT)) {
                            if (isForegroundViewIsOpen) {
                                mDownX += scaledTouchSlope;
                            } else {
                                mDownX -= scaledTouchSlope;
                            }
                        } else if (openDirection.equals(OpenDirection.TO_RIGHT)) {
                            if (isForegroundViewIsOpen) {
                                mDownX -= scaledTouchSlope;
                            } else {
                                mDownX += scaledTouchSlope;
                            }
                        }
                    }
                    break;

                case VERTICAL_MOVE:
                    handleTouchUpDown(event);
                    break;

                case HORIZONTAL_MOVE:
                    // We can swipe view to left or right only when it is possible.
                    // Else we set state to IDLE to handle other cases.
                    if (openDirection != OpenDirection.NONE) {
                        if (isForegroundViewIsOpen) {
                            //Browse.Logger.i("handleTouchOnOpenForegroundView");
                            handleTouchOnOpenForegroundView(event);
                        } else {
                            //Browse.Logger.i("handleTouchOnCloseForegroundView");
                            handleTouchOnCloseForegroundView(event);
                        }
                    } else {
                        setCurrentWrapperState(WrapperState.IDLE);
                    }
                    break;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // If foreground view is open then we must steal motion event,
        // to watch out when we should start closing foreground view.
        if (isForegroundViewIsOpen) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mInterceptEventX = (int) ev.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(ev.getRawX() - mInterceptEventX) > scaledTouchSlope) {
                        MotionEvent helper = MotionEvent.obtain(ev);
                        helper.setAction(MotionEvent.ACTION_DOWN);
                        onTouchEvent(helper);
                        return true;
                    } else {
                        return false;
                    }
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility) {
        super.dispatchWindowVisibilityChanged(visibility);
        // View.GONE = 8;
        // View.INVISIBLE = 4;
        // View.VISIBLE = 0;
        // Unregister self from wrappers array list.
        if (/*visibility == View.INVISIBLE ||*/ visibility == View.GONE) {
            sWrappers.remove(this);
        } else if (/*visibility == View.INVISIBLE ||*/ visibility == View.VISIBLE) {
            sWrappers.add(this);
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

    public void setMoveDirection(MoveDirection moveDirection) {
        this.moveDirection = moveDirection;
    }

    public void setOnLevelUpListener(OnLevelUpListener onLevelUpListener) {
        this.onLevelUpListener = onLevelUpListener;
    }

    public void setOnLevelDownListener(OnLevelDownListener onLevelDownListener) {
        this.onLevelDownListener = onLevelDownListener;
    }


    public void setOnMoveUpProgressListener(OnMoveUpProgressListener onMoveUpProgressListener) {
        this.onMoveUpProgressListener = onMoveUpProgressListener;
    }

    public void setOnMoveDownProgressListener(OnMoveDownProgressListener onMoveDownProgressListener) {
        this.onMoveDownProgressListener = onMoveDownProgressListener;
    }


    public static void setOnHoldDuringMoveUpListener(OnHoldDuringMoveUpListener onHoldDuringMoveUpListener) {
        UserViewWrapper.onHoldDuringMoveUpListener = onHoldDuringMoveUpListener;
    }

    public static void setOnHoldDuringMoveDownListener(OnHoldDuringMoveDownListener onHoldDuringMoveDownListener) {
        UserViewWrapper.onHoldDuringMoveDownListener = onHoldDuringMoveDownListener;
    }


    public static void setOnLevelUpListenerClassListener(OnLevelUpListener onLevelUpListenerClassListener) {
        UserViewWrapper.onLevelUpListenerClassListener = onLevelUpListenerClassListener;
    }

    public static void setOnLevelDownListenerClassListener(OnLevelDownListener onLevelDownListenerClassListener) {
        UserViewWrapper.onLevelDownListenerClassListener = onLevelDownListenerClassListener;
    }


    public static void setOnGoUpThresholdAchievedListener(OnGoUpThresholdAchievedListener onGoUpThresholdAchievedListener) {
        UserViewWrapper.onGoUpThresholdAchievedListener = onGoUpThresholdAchievedListener;
    }

    public static void setOnGoDownThresholdAchievedListener(OnGoDownThresholdAchievedListener onGoDownThresholdAchievedListener) {
        UserViewWrapper.onGoDownThresholdAchievedListener = onGoDownThresholdAchievedListener;
    }


    public static void setOnViewsAreAtTheTopOfTheScreenListener(OnViewsAreAtTheTopOfTheScreenListener onViewsAreAtTheTopOfTheScreenListener) {
        UserViewWrapper.onViewsAreAtTheTopOfTheScreenListener = onViewsAreAtTheTopOfTheScreenListener;
    }

    public static void setOnViewsAreAtTheBottomOfTheScreenListener(OnViewsAreAtTheBottomOfTheScreenListener onViewsAreAtTheBottomOfTheScreenListener) {
        UserViewWrapper.onViewsAreAtTheBottomOfTheScreenListener = onViewsAreAtTheBottomOfTheScreenListener;
    }


    public void setSlideUpDownViewsMs(long slideUpDownViewsMs) {
        this.slideUpDownViewsMs = slideUpDownViewsMs;
    }

    public void setOpenCloseViewDurationMs(long openCloseViewDurationMs) {
        this.openCloseViewDurationMs = openCloseViewDurationMs;
    }

    public void setTreeViewPaddingDip(long treeViewPaddingDip) {
        this.treeViewPaddingPx = getPixels((int) treeViewPaddingDip);
    }

    public void setOpenThresholdPx(long openThresholdPx) {
        this.openThresholdPx = openThresholdPx;
    }

    public void setCloseThresholdPx(long closeThresholdPx) {
        this.closeThresholdPx = closeThresholdPx;
    }

    public void setGoUpThresholdPx(long goUpThresholdPx) {
        this.goUpThresholdPx = goUpThresholdPx;
    }

    public void setGoDownThresholdPx(long goDownThresholdPx) {
        this.goDownThresholdPx = goDownThresholdPx;
    }


    private void setForegroundViewIsOpen(boolean foregroundViewIsOpen) {
        isForegroundViewIsOpen = foregroundViewIsOpen;
        userBackgroundView.setEnableViews(foregroundViewIsOpen);
    }


    /**
     * Callback is fired when other wrapper want to tell us something.
     *
     * @param callbackMsg What kind of message we arrived.
     */
    private void callbackFromOtherWrapperViews(int callbackMsg) {
        // Browse.Logger.i("Current view: " + wrapperUserType + ", callbackMsg: " + callbackMsg);

        switch (callbackMsg) {
            case CallbackMsg.SET_INVISIBLE:
//                Browse.Logger.i("SET_INVISIBLE");
                setVisibility(View.INVISIBLE);
                break;

            case CallbackMsg.SET_VISIBLE:
//                Browse.Logger.i("SET_VISIBLE");
                setVisibility(View.VISIBLE);
                break;

            case CallbackMsg.MOVEMENT_THRESHOLD_UP_ACHIEVED:
//                Browse.Logger.i("MOVEMENT_THRESHOLD_UP_ACHIEVED");
                if (onLevelUpListener != null) {
                    onLevelUpListener.onLevelUp(userBackgroundView, userForegroundView);
                }
                break;

            case CallbackMsg.MOVEMENT_THRESHOLD_DOWN_ACHIEVED:
//                Browse.Logger.i("MOVEMENT_THRESHOLD_DOWN_ACHIEVED");
                if (onLevelDownListener != null) {
                    onLevelDownListener.onLevelDown(userBackgroundView, userForegroundView);
                }
                break;

            case CallbackMsg.LEVEL_UP:
//                Browse.Logger.i("LEVEL_UP");
                break;

            case CallbackMsg.LEVEL_DOWN:
//                Browse.Logger.i("LEVEL_DOWN");
                break;

            case CallbackMsg.MOVE_TO_ORIGINAL_PLACE_FROM_BOTTOM:
                int fromBottom = ((ViewGroup) getParent()).getMeasuredHeight() + getTop();
//                Browse.Logger.i("MOVE_TO_ORIGINAL_PLACE_FROM_BOTTOM");
                moveViewBackToOriginalPlaceFrom(this, fromBottom);
                break;

            case CallbackMsg.MOVE_TO_ORIGINAL_PLACE_FROM_TOP:
                int fromTop = getMeasuredHeight() + getTop();
//                Browse.Logger.i("MOVE_TO_ORIGINAL_PLACE_FROM_TOP");
                moveViewBackToOriginalPlaceFrom(this, -fromTop);
                break;

        }

    }

    /**
     * Notify rest of wrappers about something.
     * Message describe CallbackMsg class.
     *
     * @param callbackMsg What kind of message will be send.
     */
    private void sendMsgToRestOfWrappers(int callbackMsg, boolean forAll) {
        for (UserViewWrapper userViewWrapper : sWrappers) {
            if (forAll || !userViewWrapper.wrapperUserType.equals(wrapperUserType)) {
                userViewWrapper.callbackFromOtherWrapperViews(callbackMsg);
            }
        }
    }


    private void handleTouchOnOpenForegroundView(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {

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
                                    super.onAnimationEnd(animator);
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
                                    super.onAnimationEnd(animator);
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
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(false);
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
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(true);
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

        switch (motionEvent.getAction()) {
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
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(true);
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
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(false);
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
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(true);
                                }

                            };
                            swipeAnimation(userForegroundView, updateListener, animatorListener, userForegroundView.getMeasuredWidth());
                        } else {
                            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    userBackgroundView.setVisibleWidth(userBackgroundView.getLeft(), Math.abs((Float) valueAnimator.getAnimatedValue()));
                                }
                            };
                            BetterAnimatorListener animatorListener = new BetterAnimatorListener() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    super.onAnimationEnd(animator);
                                    setForegroundViewIsOpen(false);
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

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (motionEvent.getRawY() - mDownY);
                int parentHeight = ((ViewGroup) getParent()).getMeasuredHeight();
                int topEdgeConstrain = (int) (treeViewPaddingPx - mOriginalViewTop);
                int bottomEdgeConstrain = (int) (parentHeight - mOriginalViewBottom - treeViewPaddingPx);

                if (!isViewCanBeMovedOutsideScreen) {   // Forbid move outside top edge.
                    if (dy < topEdgeConstrain) {
                        dy = topEdgeConstrain;

                    } else if (dy > bottomEdgeConstrain) {   // Forbid move outside bottom edge.
                        dy = bottomEdgeConstrain;
                    }
                }

                // Determine move direction of view. See MoveDirection enum.
                switch (moveDirection) {
                    case UP:
                        if (Math.signum(dy) != -1) {
                            return;
                        } else {
                            if (Math.abs(dy) > goUpThresholdPx) {
                                if (!isGoUpThresholdAchieved) {
                                    if (onGoUpThresholdAchievedListener != null) {
                                        // Firstly notify once through static variables.
                                        onGoUpThresholdAchievedListener.onThresholdAchieved(userBackgroundView, userForegroundView);
                                    }

                                    // Secondly notify rest of wrappers.
                                    sendMsgToRestOfWrappers(CallbackMsg.MOVEMENT_THRESHOLD_UP_ACHIEVED, false);

                                    isGoUpThresholdAchieved = true;
                                    countDownTimerDuringUpMove.start();
                                }
                            } else {
                                // Notify listener about go up progress.
                                if (onMoveUpProgressListener != null) {
                                    onMoveUpProgressListener.moveUpProgress((float) Math.abs(dy) / goUpThresholdPx);
                                }

                                if (onGoUpThresholdAchievedListener != null) {
                                    // Firstly notify once through static variables.
                                    onGoUpThresholdAchievedListener.onThresholdCancel(userBackgroundView, userForegroundView);
                                }

                                countDownTimerDuringUpMove.cancel();
                                isGoUpThresholdAchieved = false;
                            }
                        }
                        break;
                    case DOWN:
                        if (Math.signum(dy) != 1) {
                            return;
                        } else {
                            if (Math.abs(dy) > goDownThresholdPx) {
                                if (!isGoDownThresholdAchieved) {
                                    if (onGoDownThresholdAchievedListener != null) {
                                        // Firstly notify once through static variables.
                                        onGoDownThresholdAchievedListener.onThresholdAchieved(userBackgroundView, userForegroundView);
                                    }

                                    // Secondly notify rest of wrappers.
                                    sendMsgToRestOfWrappers(CallbackMsg.MOVEMENT_THRESHOLD_DOWN_ACHIEVED, false);

                                    isGoDownThresholdAchieved = true;
                                    countDownTimerDuringDownMove.start();
                                }
                            } else {
                                // Notify listener about go down progress.
                                if (onMoveDownProgressListener != null) {
                                    onMoveDownProgressListener.moveDownProgress((float) Math.abs(dy) / goDownThresholdPx);
                                }

                                if (onGoDownThresholdAchievedListener != null) {
                                    // Firstly notify once through static variables.
                                    onGoDownThresholdAchievedListener.onThresholdCancel(userBackgroundView, userForegroundView);
                                }

                                countDownTimerDuringDownMove.cancel();
                                isGoDownThresholdAchieved = false;
                            }
                        }
                        break;
                    case BOTH:
                        // Nothing.
                        break;
                    case NONE:
                        dy = 0;
                        break;
                }


                ViewHelper.setTranslationY(this, dy);
                break;

            case MotionEvent.ACTION_UP:
                // Below workaround for handle quick show wrapper when it can be move.
                // Previously was like that, view can't move, other wrappers was visible in duration time,
                // which was bad.
                if (ViewHelper.getTranslationY(this) != 0) {

                    if (isGoUpThresholdAchieved) {

                        countDownTimerDuringUpMove.cancel();
                        createObjectAnimatorHelperY(this, new BetterAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                super.onAnimationEnd(animator);

                                // In this place, view is outside the screen.
                                // Here we can prepare changing data on the views.
                                if (onViewsAreAtTheBottomOfTheScreenListener != null) {
                                    onViewsAreAtTheBottomOfTheScreenListener.onViewsAreAtTheBottomOfTheScreen(wrapperUserType);
                                }

                                sendMsgToRestOfWrappers(CallbackMsg.MOVE_TO_ORIGINAL_PLACE_FROM_BOTTOM, true);
                            }
                        }, -1500).start();

                        sendMsgToRestOfWrappers(CallbackMsg.LEVEL_UP, false);

                        if (onLevelUpListenerClassListener != null) {
                            onLevelUpListenerClassListener.onLevelUp(userBackgroundView, userForegroundView);
                        }


                    } else if (isGoDownThresholdAchieved) {

                        countDownTimerDuringDownMove.cancel();
                        createObjectAnimatorHelperY(this, new BetterAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                super.onAnimationEnd(animator);

                                // In this place, view is outside the screen.
                                // Here we can prepare changing data on the views.
                                if (onViewsAreAtTheTopOfTheScreenListener != null) {
                                    onViewsAreAtTheTopOfTheScreenListener.onViewsAreAtTheTopOfTheScreen(wrapperUserType);
                                }

                                sendMsgToRestOfWrappers(CallbackMsg.MOVE_TO_ORIGINAL_PLACE_FROM_TOP, true);
                            }
                        }, 1500).start();

                        sendMsgToRestOfWrappers(CallbackMsg.LEVEL_DOWN, false);

                        if (onLevelDownListenerClassListener != null) {
                            onLevelDownListenerClassListener.onLevelDown(userBackgroundView, userForegroundView);
                        }

                    } else {
                        moveViewBackToOriginalPlace(this, new BetterAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                super.onAnimationEnd(animator);
                                sendMsgToRestOfWrappers(CallbackMsg.SET_VISIBLE, false);
                            }
                        }, 0);
                    }

                } else {
                    setCurrentWrapperState(WrapperState.IDLE);
                    sendMsgToRestOfWrappers(CallbackMsg.SET_VISIBLE, false);
                }
                break;
        }

    }


    private void swipeAnimation(final View swipeView, ValueAnimator.AnimatorUpdateListener animatorListener, Animator.AnimatorListener listener, float translation) {
        ObjectAnimator objectAnimator = createObjectAnimatorHelperX(swipeView, listener, translation);
        objectAnimator.setEvaluator(new FloatEvaluator());
        objectAnimator.addUpdateListener(animatorListener);
        objectAnimator.start();
    }

    private void cancelAnimation(final View swipeView, ValueAnimator.AnimatorUpdateListener animatorListener, Animator.AnimatorListener listener, float translation) {
        ObjectAnimator cancelTranslationX = createObjectAnimatorHelperX(swipeView, listener, translation);
        cancelTranslationX.addUpdateListener(animatorListener);
        cancelTranslationX.setEvaluator(new FloatEvaluator());
        cancelTranslationX.start();
    }

    private void moveViewBackToOriginalPlace(final View view, Animator.AnimatorListener listener, float translation) {
        createObjectAnimatorHelperY(view, listener, translation).start();
    }

    private void moveViewBackToOriginalPlaceFrom(final View view, float from) {
        createObjectAnimatorHelperY(view, new BetterAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                sendMsgToRestOfWrappers(CallbackMsg.SET_VISIBLE, true);
            }
        }, from, 0).start();
    }


    private ObjectAnimator createObjectAnimatorHelperY(final View view, Animator.AnimatorListener listener, float... values) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", values);
        objectAnimator.setDuration(slideUpDownViewsMs);
        objectAnimator.addListener(listener);
        return objectAnimator;
    }

    private ObjectAnimator createObjectAnimatorHelperX(final View view, Animator.AnimatorListener listener, float... values) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationX", values);
        objectAnimator.setDuration(openCloseViewDurationMs);
        objectAnimator.addListener(listener);
        return objectAnimator;
    }


    private WrapperState determineWrapperState(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // mDown's variables and mCurrentTransx is use to do calculations below and in methods handle(Open/Close)ForegroundView.
                mDownX = (int) motionEvent.getRawX();
                mDownY = (int) motionEvent.getRawY();
                mCurentTransX = ViewHelper.getTranslationX(userForegroundView);
                mOriginalViewTop = getTop();
                mOriginalViewBottom = getBottom();
                break;

            case MotionEvent.ACTION_MOVE:
                int currentX = (int) motionEvent.getRawX();
                int currentY = (int) motionEvent.getRawY();
                int dx = Math.abs(currentX - mDownX);
                int dy = Math.abs(currentY - mDownY);

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

    private void setCurrentWrapperState(WrapperState wrapperState) {
        currentWrapperState = wrapperState;
    }

    private void setPreviousWrapperState(WrapperState wrapperState) {
        previousWrapperState = wrapperState;
    }


    private void init(Context context, AttributeSet attributeSet) {
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

        // Determine user view type.
        if (attributeSet != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.UserViewWrapperAttrs);
            int attrsCount = typedArray.getIndexCount();

            for (int i = 0; i < attrsCount; i++) {
                int attribute = typedArray.getIndex(i);

                switch (attribute) {
                    case R.styleable.UserViewWrapperAttrs_user_type:
                        // Get user type from xml attribute.
                        switch (typedArray.getInt(attribute, 1)) {
                            // Parent.
                            case 1:
                                wrapperUserType = WrapperUserType.PARENT;
                                break;
                            // Left child.
                            case 2:
                                wrapperUserType = WrapperUserType.LEFT_CHILD;
                                break;
                            // Right child.
                            case 3:
                                wrapperUserType = WrapperUserType.RIGHT_CHILD;
                                break;
                        }
                        break;
                    case R.styleable.UserViewWrapperAttrs_open_direction:
                        // Get open type from xml attribute.
                        switch (typedArray.getInt(attribute, 3)) {
                            // Parent.
                            case 1:
                                openDirection = OpenDirection.TO_LEFT;
                                break;
                            // Left child.
                            case 2:
                                openDirection = OpenDirection.TO_RIGHT;
                                break;
                            // Right child.
                            case 3:
                                openDirection = OpenDirection.NONE;
                                break;
                        }
                        break;

                    case R.styleable.UserViewWrapperAttrs_outside_move:
                        // Get outside move ability from xml attribute.
                        isViewCanBeMovedOutsideScreen = typedArray.getBoolean(attribute, false);
                        break;

                    case R.styleable.UserViewWrapperAttrs_move_direction:
                        // Get move type from xml attribute.
                        switch (typedArray.getInt(attribute, 3)) {
                            case 1:
                                moveDirection = MoveDirection.UP;
                                break;
                            case 2:
                                moveDirection = MoveDirection.DOWN;
                                break;
                            case 3:
                                moveDirection = MoveDirection.BOTH;
                                break;
                            case 4:
                                moveDirection = MoveDirection.NONE;
                                break;
                        }
                        break;
                }
            }

        }


        // Initialize constants from resources.
        slideUpDownViewsMs = 460;
        openCloseViewDurationMs = 300;
        scaledTouchSlope = ViewConfiguration.get(context).getScaledTouchSlop();
        treeViewPaddingPx = getPixels(8);
        openThresholdPx = 200;
        closeThresholdPx = 200;
        goDownThresholdPx = 200;
        goUpThresholdPx = 200;

        // Register self as listener for messages from other wrappers.
        sWrappers.add(this);

        countDownTimerDuringUpMove = new CountDownTimer(1500, 25) {
            @Override
            public void onTick(long l) {
//                Browse.Logger.i("onTick - up:" + l);
                if (onHoldDuringMoveUpListener != null) {
                    onHoldDuringMoveUpListener.onTick(l);
                }
            }

            @Override
            public void onFinish() {
//                Browse.Logger.i("onFinish - up");
                if (onHoldDuringMoveUpListener != null) {
                    onHoldDuringMoveUpListener.onFinish();
                }
            }
        };
        countDownTimerDuringDownMove = new CountDownTimer(1500, 25) {
            @Override
            public void onTick(long l) {
//                Browse.Logger.i("onTick - down:" + l);
                if (onHoldDuringMoveDownListener != null) {
                    onHoldDuringMoveDownListener.onTick(l);
                }
            }

            @Override
            public void onFinish() {
//                Browse.Logger.i("onFinish - down");
                if (onHoldDuringMoveDownListener != null) {
                    onHoldDuringMoveDownListener.onFinish();
                }
            }
        };
    }

    private int getDip(int pixel) {
        return (pixel * 160 / getResources().getDisplayMetrics().densityDpi);
    }

    private int getPixels(int dip) {
        return (getResources().getDisplayMetrics().densityDpi * dip / 160);
    }

    /**
     * Helper class to avoid create useless methods in base Listener interface.
     */
    private class BetterAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {
            setCurrentWrapperState(WrapperState.ANIMATING);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            setCurrentWrapperState(WrapperState.IDLE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    /**
     * Class describes constants types of messages which are can be send to other wrappers.
     */
    private static class CallbackMsg {
        // Tell others wrappers to change itself visibility to INVISIBLE.
        public static final int SET_INVISIBLE = 1 << 1;
        // Tell others wrappers to change itself visibility to VISIBLE.
        public static final int SET_VISIBLE = 1 << 2;
        // Tell others wrappers that some threshold was achieved.
        public static final int MOVEMENT_THRESHOLD_UP_ACHIEVED = 1 << 3;
        public static final int MOVEMENT_THRESHOLD_DOWN_ACHIEVED = 1 << 4;

        public static final int LEVEL_UP = 1 << 5;
        public static final int LEVEL_DOWN = 1 << 6;

        public static final int MOVE_TO_ORIGINAL_PLACE_FROM_TOP = 1 << 7;
        public static final int MOVE_TO_ORIGINAL_PLACE_FROM_BOTTOM = 1 << 8;

    }
}
