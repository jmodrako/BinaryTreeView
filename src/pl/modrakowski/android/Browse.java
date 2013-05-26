package pl.modrakowski.android;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.googlecode.androidannotations.annotations.*;

@NoTitle
@EActivity(R.layout.main)
public class Browse extends Activity {

    @ViewById(R.id.parent)
    protected UserViewWrapper parentLayoutWrapper;

    @ViewById(R.id.left_child)
    protected UserViewWrapper leftLayoutWrapper;

    @ViewById(R.id.right_child)
    protected UserViewWrapper rightLayoutWrapper;

    @ViewById(R.id.to_left)
    protected Button toLeft;

    @ViewById(R.id.to_right)
    protected Button toRight;

    @ViewById(R.id.test_btn)
    Button testBtn;

    @ViewById(R.id.tree_txt_indicator)
    CustomFontTextView indicator;

    @AfterInject
    protected void afterInject() {
        getWindow().setBackgroundDrawable(null);
    }

    @AfterViews
    protected void afterViews() {

        UserViewWrapper.setGoUpThresholdAchievedCallback(new UserViewWrapper.GoUpThresholdAchievedCallback() {
            @Override
            public void goUpThresholdAchievedCallback(ViewGroup backgroundView, ViewGroup foregroundView) {
                //indicator.setText("Release to go up.");
            }

            @Override
            public void goUpThresholdCancelCallback(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }
        });
        UserViewWrapper.setGoDownThresholdAchievedCallback(new UserViewWrapper.GoDownThresholdAchievedCallback() {
            @Override
            public void goDownThresholdAchievedCallback(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("Release to go down.");
            }

            @Override
            public void goDownThresholdCancelCallback(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }
        });

        UserViewWrapper.setCallbackMoveUpClassListener(new UserViewWrapper.CallbackMoveUp() {
            @Override
            public void callbackUp(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }

            @Override
            public void callbackUpCancel(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }
        });
        UserViewWrapper.setCallbackMoveDownClassListener(new UserViewWrapper.CallbackMoveDown() {
            @Override
            public void callbackDown(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }

            @Override
            public void callbackDownCancel(ViewGroup backgroundView, ViewGroup foregroundView) {
                indicator.setText("");
            }
        });

        UserViewWrapper.setLongHoldDuringUpMoveListener(new UserViewWrapper.LongHoldDuringUpMoveListener() {
            @Override
            public void onTick(long progress) {
                indicator.setText(String.format("Release to go down. Time: %d s ", progress));
            }

            @Override
            public void onFinish() {
                indicator.setText("Release to go to the bottom of tree.");
            }
        });

        UserViewWrapper.setLongHoldDuringDownMoveListener(new UserViewWrapper.LongHoldDuringDownMoveListener() {
            @Override
            public void onTick(long progress) {
                indicator.setText(String.format("Release to go up. Time: %d s ", progress));
            }

            @Override
            public void onFinish() {
                indicator.setText("Release to go to the top of tree.");
            }
        });
    }

    @Click(R.id.test_btn)
    protected void onLeftClick() {
        Logger.i("test button message...................");
    }


    @Click(R.id.prompt)
    protected void onPromptClick() {
        parentLayoutWrapper.startPromptAnimation(false, 100);
        leftLayoutWrapper.startPromptAnimation(false, 130);
        rightLayoutWrapper.startPromptAnimation(false, 160);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        EasyTracker.getTracker().sendView("/Browse");
        GAServiceManager.getInstance().dispatch();
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    public static class Logger {
        public static void i(String message) {
            Log.i("BROWSE_TEST", message);
        }
    }
}
