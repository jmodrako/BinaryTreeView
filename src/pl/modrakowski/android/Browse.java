package pl.modrakowski.android;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import com.googlecode.androidannotations.annotations.*;

@NoTitle
@EActivity(R.layout.main)
public class Browse extends Activity {

    /*@ViewById(R.id.background_ctn)
    protected CuriousContainer background;

    @ViewById(R.id.foreground_ctn)
    protected CuriousContainer foreground;*/

    /*@AfterViews
    protected void afterViews() {
        *//*background.setTAG("background");
        foreground.setTAG("foreground");*//*
    }*/

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
        /*TextView parentText = (TextView) parentLayoutWrapper.findViewById(R.id.name);
        TextView leftText = (TextView) leftLayoutWrapper.findViewById(R.id.name);
        TextView rightText = (TextView) rightLayoutWrapper.findViewById(R.id.name);

        parentText.setText("PARENT");
        leftText.setText("LEFT");
        rightText.setText("RIGHT");*/
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

    public static class Logger {
        public static void i(String message) {
            Log.i("BROWSE_TEST", message);
        }
    }
}
