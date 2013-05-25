package pl.modrakowski.android;

import android.app.Activity;
import android.util.Log;
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

    @AfterInject
    protected void afterInject() {
        getWindow().setBackgroundDrawable(null);
    }

    @AfterViews
    protected void afterViews() {

    }

    @Click(R.id.to_left)
    protected void onLeftClick() {
        parentLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_LEFT);
        rightLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_LEFT);
        leftLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_LEFT);
    }

    @Click(R.id.to_right)
    protected void onRightClick() {
        parentLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_RIGHT);
        rightLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_RIGHT);
        leftLayoutWrapper.setOpenDirection(UserViewWrapper.OpenDirection.TO_RIGHT);
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
