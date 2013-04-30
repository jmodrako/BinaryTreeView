package pl.modrakowski.android;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

public class TypefacesUtils {

    private static final Hashtable<String, Typeface> CACHE = new Hashtable<String, Typeface>();

    public static Typeface get(String typefaceName, Context context) {
        synchronized (CACHE) {
            if (!CACHE.containsKey(typefaceName)) {
                try {
                    CACHE.put(typefaceName, Typeface.createFromAsset(context.getAssets(), "Fonts/" + typefaceName));
                } catch (Exception e) {
                    Log.e("Typeface", "Could not get typeface '" + typefaceName + "' because " + e.getMessage());
                    return null;
                }
            }
            return CACHE.get(typefaceName);
        }
    }

    public synchronized static Hashtable<String, Typeface> getAllTypefaces() {
        return CACHE;
    }
}


