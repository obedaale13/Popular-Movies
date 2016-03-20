package danielkaparunakis.popularmovies;

import android.app.Application;
import android.content.Context;

/**
 * Created by DanielKaparunakis on 3/15/16.
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

