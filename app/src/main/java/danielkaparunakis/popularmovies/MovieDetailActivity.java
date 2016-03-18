package danielkaparunakis.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity {

    //Constants
    private final String MOVIE_ID         = "id";
    private final String ORIGINAL_TITLE   = "original_title";
    private final String POSTER_PATH      = "poster_path";
    private final String OVERVIEW         = "overview";
    private final String VOTE_AVERAGE     = "vote_average";
    private final String RELEASE_DATE     = "release_date";
    private final String IS_FAVORITE_MODE = "isFavoriteMode";
    private final String POSTER_FULL_PATH = "http://image.tmdb.org/t/p/w500";
    private final String MAX_VOTE_AVERAGE = "/10";
    private final String LOG_TAG          = MovieDetailActivity.class.getSimpleName();
    ImageView movieThumbnail;
    private Cursor cursor;
    private ArrayList<String> movieTrailerReviewDataAL = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String mOriginalTitle;
        String mPosterPath;
        String mOverview;
        String mReleaseDate;
        String mVoteAverage;
        boolean isFavorite;

        //Receive intent data
        Intent movieDetailActivity = getIntent();
        ContentResolver resolver = getContentResolver();

        if (movieDetailActivity.getBooleanExtra(IS_FAVORITE_MODE, false)){
            cursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                    MovieContract.MovieTable.projection,
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)},
                    null);
            isFavorite = cursor.moveToFirst();
            mOriginalTitle = cursor.getString(2);
            mPosterPath = cursor.getString(3);
            mOverview = cursor.getString(4);
            mReleaseDate = cursor.getString(6);
            mVoteAverage = cursor.getString(5);
        } else {
            cursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                    new String[]{MovieContract.MovieTable._ID, MovieContract.MovieTable.COLUMN_MOVIE_ID, MovieContract.MovieTable.COLUMN_POSTER_PATH},
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)},
                    null);
            isFavorite = cursor.moveToFirst();
            mOriginalTitle = movieDetailActivity.getStringExtra(ORIGINAL_TITLE);
            mPosterPath = POSTER_FULL_PATH + movieDetailActivity.getStringExtra(POSTER_PATH);
            mOverview = movieDetailActivity.getStringExtra(OVERVIEW);
            mReleaseDate = movieDetailActivity.getStringExtra(RELEASE_DATE);
            mVoteAverage = movieDetailActivity.getStringExtra(VOTE_AVERAGE);
            new FetchDetailMovieData().execute(movieDetailActivity.getStringExtra(MOVIE_ID));
        }

        getSupportActionBar().setTitle(mOriginalTitle);

        //Update movie thumbnail ImageView
        movieThumbnail = (ImageView) findViewById(R.id.image_movie_thumbnail);
        movieThumbnail.setMaxHeight(750);
        movieThumbnail.setMaxWidth(500);
        movieThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (movieDetailActivity.getBooleanExtra(IS_FAVORITE_MODE, false)){
            Picasso.with(this)
                    .load(new File(mPosterPath))
                    .into(movieThumbnail);
        } else {
            Picasso.with(this)
                    .load(mPosterPath)
                    .into(movieThumbnail);
        }


        //Update overview Textview
        TextView overview = (TextView) findViewById(R.id.text_overview);
        overview.setText(mOverview);

        String newReleaseDate = null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(mReleaseDate);
            newReleaseDate = new SimpleDateFormat("yyyy").format(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Parse error");
        }

        TextView releaseDate = (TextView) findViewById(R.id.text_release_date);
        releaseDate.setText(newReleaseDate);

        //Update vote average textview
        TextView voteAverage = (TextView) findViewById(R.id.text_vote_average);
        voteAverage.setText(mVoteAverage + MAX_VOTE_AVERAGE);

        CheckBox favoriteCheckbox = (CheckBox) findViewById(R.id.checkbox_mark_as_favorite);
        favoriteCheckbox.setChecked(isFavorite);
    }

    private String getValidFileName(String invalidName){
        String validName = invalidName.replaceAll(":","");
        return validName;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onFavoriteClicked(View view){
        CheckBox favoriteCheckbox = (CheckBox) findViewById(R.id.checkbox_mark_as_favorite);
        ContentResolver resolver = getContentResolver();
        Intent movieDetailActivity = getIntent();

        if(favoriteCheckbox.isChecked()){
            ContentValues contentValues = new ContentValues();
            Bitmap b = Bitmap.createBitmap(movieThumbnail.getWidth(),movieThumbnail.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            movieThumbnail.draw(canvas);
            OutputStream fOut;
            String path = Environment.getExternalStorageDirectory().toString() + "/" +
                    getValidFileName(movieDetailActivity.getStringExtra(ORIGINAL_TITLE)) + ".jpg";
            File file = new File(path);
            try {
                fOut = new FileOutputStream(file);
                b.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
            } catch (IOException e){
                e.printStackTrace();
            }
            contentValues.put(MovieContract.MovieTable.COLUMN_MOVIE_ID,
                    movieDetailActivity.getStringExtra(MOVIE_ID));
            contentValues.put(MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE,
                    movieDetailActivity.getStringExtra(ORIGINAL_TITLE));
            contentValues.put(MovieContract.MovieTable.COLUMN_POSTER_PATH, path);
            contentValues.put(MovieContract.MovieTable.COLUMN_OVERVIEW,
                    movieDetailActivity.getStringExtra(OVERVIEW));
            contentValues.put(MovieContract.MovieTable.COLUMN_VOTE_AVERAGE,
                    movieDetailActivity.getStringExtra((VOTE_AVERAGE)));
            contentValues.put(MovieContract.MovieTable.COLUMN_RELEASE_DATE,
                    movieDetailActivity.getStringExtra((RELEASE_DATE)));
            resolver.insert(MovieContract.MovieTable.CONTENT_URI, contentValues);
        } else {
            if (movieDetailActivity.getBooleanExtra(IS_FAVORITE_MODE, false)){
                File file = new File(cursor.getString(3));
                file.delete();
            } else {
                File file = new File(cursor.getString(2));
                file.delete();
            }
            resolver.delete(MovieContract.MovieTable.CONTENT_URI,
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)});
        }

    }

    public void onYoutubeClicked(View view){
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + movieTrailerReviewDataAL.get(0)));
        startActivity(youtubeIntent);
    }

    public class FetchDetailMovieData extends AsyncTask<String, Void, String[]> {

        private final String BUILDER_SCHEME = "http";
        private final String BUILDER_AUTHORITY = "api.themoviedb.org";
        private final String BUILDER_PATH_1 = "3";
        private final String BUILDER_PATH_2 = "movie";
        private final String APIKEY_PARAM = "api_key";
        private final String TRAILERS_REVIEWS = "append_to_response";
        private final String LOG_TAG = FetchDetailMovieData.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            // Will contain the raw JSON response as a string.
            String movieDataJSONStr = null;

            //API call URL builder
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(BUILDER_SCHEME)
                    .authority(BUILDER_AUTHORITY)
                    .appendPath(BUILDER_PATH_1)
                    .appendPath(BUILDER_PATH_2)
                    .appendPath(params[0])
                    .appendQueryParameter(APIKEY_PARAM, "***REMOVED***")
                    .appendQueryParameter(TRAILERS_REVIEWS, "reviews,trailers");

            //Built URL stored in a string
            String builtURL = builder.build().toString();

            try {

                // Construct the URL for the API query
                URL url = new URL(builtURL);
                // Create the request to TheMovieDB, and open the connection
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                //Read the input stream into a String
                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    //no data obtained, nothing to do
                    movieDataJSONStr = null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line = bufferedReader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    stringBuffer.append(line + "\n");
                }

                if (stringBuffer.length() == 0) {
                    //string was empty
                    movieDataJSONStr = null;
                }
                movieDataJSONStr = stringBuffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                movieDataJSONStr = null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            //Attempt to parse data
            try {
                return getMovieDataFromJSONStr(movieDataJSONStr);
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] movieTrailerReviewData) {
            super.onPostExecute(movieTrailerReviewData);

            //Take data received from the execution of Asynctask and update an ArrayList with it
            //Then update the image adapter
            if (movieTrailerReviewData != null){
                movieTrailerReviewDataAL.clear();
                movieTrailerReviewDataAL.addAll(Arrays.asList(movieTrailerReviewData));
                for (int i = 0; i < movieTrailerReviewDataAL.size(); i++){
                    Log.v("TACOS", movieTrailerReviewDataAL.get(i));
                }
            }
            TextView reviews = (TextView) findViewById(R.id.textview_review);
            reviews.setText(movieTrailerReviewDataAL.get(1) + "\n\n" + movieTrailerReviewDataAL.get(2));
        }

        private String[] getMovieDataFromJSONStr(String JSONRawData) throws JSONException {

            JSONObject movieDetailJObj;
            if (JSONRawData != null) {
                //Turns raw string data into a JSON object
                movieDetailJObj = new JSONObject(JSONRawData);
            } else {
                return null;
            }

            //pulls resuts array
            JSONArray movieTrailerData = movieDetailJObj.getJSONObject("trailers").getJSONArray("youtube");
            JSONArray movieReviewData = movieDetailJObj.getJSONObject("reviews").getJSONArray("results");
            //pulls poster paths and stores them in an array
            String[] movieTrailerReviewData = new String[movieTrailerData.length() + movieReviewData.length()];
            for (int i = 0; i < movieTrailerData.length(); i++) {
                movieTrailerReviewData[i] = movieTrailerData.getJSONObject(i).getString("source");
            }
            for (int i = 0; i < movieReviewData.length(); i++) {
                movieTrailerReviewData[i + movieTrailerData.length()] = movieReviewData.getJSONObject(i).getString("content");
            }
            return movieTrailerReviewData;

        }
    }

}
