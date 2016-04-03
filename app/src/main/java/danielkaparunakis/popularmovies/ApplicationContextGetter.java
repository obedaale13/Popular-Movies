package danielkaparunakis.popularmovies;

import android.app.Application;
import android.content.Context;

/**
 * Created by DanielKaparunakis on 3/15/16.
 */

/**
 * Utility designed to provide access to the application context at all times. Used in a few places
 * where the application context is not accessible. Most importantly, the database helper file.
 */
public class ApplicationContextGetter extends Application {

    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return sContext;
    }

}

