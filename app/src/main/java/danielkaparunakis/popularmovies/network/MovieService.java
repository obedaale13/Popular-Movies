package danielkaparunakis.popularmovies.network;

import android.content.ContentResolver;
import android.database.Cursor;

import com.danielkaparunakis.googleplayapp.ApplicationContextGetter;
import com.danielkaparunakis.googleplayapp.Model.GIModel;
import com.danielkaparunakis.googleplayapp.Model.GMD;
import com.danielkaparunakis.googleplayapp.SubsonicContract;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;

/**
 * Created by DanielKaparunakis on 4/19/16.
 */
public class MovieService {

    private SubsonicAPI subsonicAPI;
    private final String BUILDER_SCHEME = "http://";

    public MovieService() {
        ContentResolver contentResolver = ApplicationContextGetter.getAppContext().getContentResolver();
        Cursor cursor = contentResolver.query(SubsonicContract.ServerTable.CONTENT_URI,
                SubsonicContract.ServerTable.projection,
                SubsonicContract.ServerTable._ID + " = ?",
                new String[]{"1"},
                null);
        cursor.moveToFirst();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                //.addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUILDER_SCHEME + cursor.getString(2) + ":" + cursor.getString(3))
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        subsonicAPI = retrofit.create(SubsonicAPI.class);
    }

    public Observable<GIModel> getIndexesView(String user, String salt, String token, String version, String app,
                                              String format) throws IOException {
        return subsonicAPI.getIndexesView(user, salt, token, version, app, format);
    }

    public Observable<GMD> getMusicDirectory(String user, String salt, String token, String version, String app,
                                             String format, String id) throws IOException {
        return subsonicAPI.getMusicDirectory(user, salt, token, version, app, format, id);
    }
}


