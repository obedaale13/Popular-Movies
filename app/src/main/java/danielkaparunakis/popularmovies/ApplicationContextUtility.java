package danielkaparunakis.popularmovies;

import android.app.Application;
import android.content.Context;

/**
 * Created by DanielKaparunakis on 3/15/16.
 */
public class ApplicationContextUtility extends Application {

    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return mContext;
    }

}

