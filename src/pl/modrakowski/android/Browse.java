package pl.modrakowski.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    @AfterInject
    protected void afterInject() {
        getWindow().setBackgroundDrawable(null);
    }

    @AfterViews
    protected void afterViews() {
        leftLayoutWrapper.setCallbackMoveUp(new UserViewWrapper.CallbackMoveUp() {
            @Override
            public void callbackUp(ViewGroup backgroundView, ViewGroup foregroundView) {
                //((TextView) foregroundView.findViewById(R.id.test_left_child_fore_tv)).setText("Zmiana tekstu. Lewy.");
            }
        });

        rightLayoutWrapper.setCallbackMoveUp(new UserViewWrapper.CallbackMoveUp() {
            @Override
            public void callbackUp(ViewGroup backgroundView, ViewGroup foregroundView) {
                //((TextView) foregroundView.findViewById(R.id.test_right_child_fore_tv)).setText("Zmiana tekstu. Prawy.");
            }
        });

        parentLayoutWrapper.setCallbackMoveUp(new UserViewWrapper.CallbackMoveUp() {
            @Override
            public void callbackUp(ViewGroup backgroundView, ViewGroup foregroundView) {
                //((TextView) foregroundView.findViewById(R.id.test_parent_child_fore_tv)).setText("Zmiana tekstu. Parent.");
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
