package danielkaparunakis.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;

/**
 * Created by DanielKaparunakis on 3/17/16.
 */
public class ConnectivityStatus {

    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ApplicationContextGetter.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static boolean isConnectionMetered() {
        ConnectivityManager cm = (ConnectivityManager) ApplicationContextGetter.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return ConnectivityManagerCompat.isActiveNetworkMetered(cm);
    }

    public static boolean isNetworkGood() {
        ConnectivityManager cm = (ConnectivityManager) ApplicationContextGetter.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }

        int netType = info.getType();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            return info.isConnectedOrConnecting();
        } else {
            return false;
        }
    }
}
