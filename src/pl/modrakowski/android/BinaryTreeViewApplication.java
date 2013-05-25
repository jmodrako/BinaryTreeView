package pl.modrakowski.android;

import android.app.Application;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * User: Jack Modrakowski
 * Date: 5/24/13
 * Time: 7:39 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class BinaryTreeViewApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Tracker tracker = GoogleAnalytics.getInstance(this).getTracker("UA-41202416-1");
        GoogleAnalytics.getInstance(this).setDefaultTracker(tracker);

        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                EasyTracker.getTracker().sendException(throwable.getLocalizedMessage(), true);
            }
        });*/
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
