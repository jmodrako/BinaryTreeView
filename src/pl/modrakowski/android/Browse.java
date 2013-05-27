package pl.modrakowski.android;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EActivity;

@EActivity(R.layout.main_activity)
public class Browse extends FragmentActivity {

    @AfterInject
    protected void afterInject() {
        getWindow().setBackgroundDrawable(null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.new_game:

                return true;
            case R.id.help:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class Logger {
        public static void i(String message) {
            Log.i("BROWSE_TEST", message);
        }
    }
}
