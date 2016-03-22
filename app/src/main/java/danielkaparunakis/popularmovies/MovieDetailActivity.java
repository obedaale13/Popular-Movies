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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    
    private final String MOVIE_ID            = "id";
    private final String ORIGINAL_TITLE      = "original_title";
    private final String POSTER_PATH         = "poster_path";
    private final String OVERVIEW            = "overview";
    private final String VOTE_AVERAGE        = "vote_average";
    private final String RELEASE_DATE        = "release_date";
    private final String POSTER_FULL_PATH    = "http://image.tmdb.org/t/p/w500";
    private final String MAX_VOTE_AVERAGE    = "/10";
    private final String TRAILER_REVIEW_LIST = "TrailerReviewArrayListKey";
    private final String LOG_TAG             = MovieDetailActivity.class.getSimpleName();

    boolean mIsFavorite;
    ImageView mMovieThumbnail;
    private Cursor mMovieDataCursor;
    private ArrayList<String> mMovieTrailerReviewDataList = new ArrayList<String>();
    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If the movie is in the database, go ahead and set the share intent right now.
        if (mIsFavorite) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMovieDataCursor.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(TRAILER_REVIEW_LIST, mMovieTrailerReviewDataList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent movieDetailActivity = getIntent();
        if (savedInstanceState != null) {
            mMovieTrailerReviewDataList = savedInstanceState.getStringArrayList(TRAILER_REVIEW_LIST);
        }

        // Query database to see if the movie sent in by the main fragment is a favorite and has all
        // its data in the database.
        ContentResolver resolver = getContentResolver();
        mMovieDataCursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                MovieContract.MovieTable.projection,
                MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)},
                null);

        mIsFavorite = mMovieDataCursor.moveToFirst();
        String originalTitle;
        String posterPath;
        String overview;
        String releaseDate;
        String voteAverage;
        TextView viewOverview = (TextView) findViewById(R.id.text_overview);
        TextView viewReleaseDate = (TextView) findViewById(R.id.text_release_date);
        TextView viewVoteAverage = (TextView) findViewById(R.id.text_vote_average);
        CheckBox favoriteCheckbox = (CheckBox) findViewById(R.id.checkbox_mark_as_favorite);
        TextView reviews = (TextView) findViewById(R.id.textview_review);

        // If the movie is found in the database, pull all the data from the database, otherwise
        // check if there was data saved in the savedInstanceBundle list, and use that or
        // pull all the data from the intent and then make the network call to obtain trailer &
        // review data. In the rare case that there is no database data or network connection or API,
        // completely hide all views and tell user to connect to the internet
        if (mIsFavorite){
            originalTitle = mMovieDataCursor.getString(2);
            posterPath = mMovieDataCursor.getString(3);
            overview = mMovieDataCursor.getString(4);
            releaseDate = mMovieDataCursor.getString(6);
            voteAverage = mMovieDataCursor.getString(5) + MAX_VOTE_AVERAGE;
        } else if (!mMovieTrailerReviewDataList.isEmpty() && movieDetailActivity.hasExtra(ORIGINAL_TITLE)){
            originalTitle = movieDetailActivity.getStringExtra(ORIGINAL_TITLE);
            posterPath = POSTER_FULL_PATH + movieDetailActivity.getStringExtra(POSTER_PATH);
            overview = movieDetailActivity.getStringExtra(OVERVIEW);
            releaseDate = movieDetailActivity.getStringExtra(RELEASE_DATE);
            voteAverage = movieDetailActivity.getStringExtra(VOTE_AVERAGE) + MAX_VOTE_AVERAGE;
            Log.e(LOG_TAG, "case 2");
        } else if (ConnectivityStatus.isOnline()){
            originalTitle = movieDetailActivity.getStringExtra(ORIGINAL_TITLE);
            posterPath = POSTER_FULL_PATH + movieDetailActivity.getStringExtra(POSTER_PATH);
            overview = movieDetailActivity.getStringExtra(OVERVIEW);
            releaseDate = movieDetailActivity.getStringExtra(RELEASE_DATE);
            voteAverage = movieDetailActivity.getStringExtra(VOTE_AVERAGE) + MAX_VOTE_AVERAGE;
            new FetchDetailMovieData().execute(movieDetailActivity.getStringExtra(MOVIE_ID));
            Log.e(LOG_TAG, "case 3");
        } else {
            originalTitle = "";
            posterPath = "";
            overview = "";
            releaseDate = "";
            voteAverage = "";
            viewOverview.setVisibility(View.INVISIBLE);
            viewReleaseDate.setVisibility(View.INVISIBLE);
            viewVoteAverage.setVisibility(View.INVISIBLE);
            favoriteCheckbox.setVisibility(View.INVISIBLE);
            reviews.setVisibility(View.INVISIBLE);
            View dividerOne = findViewById(R.id.divider_one);
            dividerOne.setVisibility(View.INVISIBLE);
            View dividerTwo = findViewById(R.id.divider_two);
            dividerTwo.setVisibility(View.INVISIBLE);
            TextView trailerLabel = (TextView) findViewById(R.id.textview_trailers_label);
            trailerLabel.setVisibility(View.INVISIBLE);
            TextView reviewLabel = (TextView) findViewById(R.id.textview_reviews_label);
            reviewLabel.setVisibility(View.INVISIBLE);
            ImageView youtubeButton = (ImageView) findViewById(R.id.imageview_youtube_play);
            youtubeButton.setVisibility(View.INVISIBLE);
        }

        // This block updates all the views with corresponding movie data using the the variables
        // initialized either with the data in the database or with the data coming from the intent.
        getSupportActionBar().setTitle(originalTitle);

        mMovieThumbnail = (ImageView) findViewById(R.id.image_movie_thumbnail);
        mMovieThumbnail.setMaxHeight(750);
        mMovieThumbnail.setMaxWidth(500);
        mMovieThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // If the movie is in the database, pull the file that was saved, otherwise load from the
        // internet using Picasso.
        if (mIsFavorite){
            Picasso.with(this)
                    .load(new File(posterPath))
                    .into(mMovieThumbnail);
        } else if (posterPath.isEmpty()){
            Toast.makeText(this, "The movie data is no longer stored offline and you don't have " +
                    "an internet connection, please connect to the internet and try again",
                    Toast.LENGTH_LONG).show();
        } else {
            Picasso.with(this)
                    .load(posterPath)
                    .into(mMovieThumbnail);
        }

        viewOverview.setText(overview);

        String newReleaseDate = null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(releaseDate);
            newReleaseDate = new SimpleDateFormat("yyyy").format(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Parse error");
        }
        viewReleaseDate.setText(newReleaseDate);

        viewVoteAverage.setText(voteAverage);

        favoriteCheckbox.setChecked(mIsFavorite);

        // If the movie is in the database, update the review & trailer views now, otherwise, make
        // an API call and update them in postExecute().
        if(mIsFavorite) {
            if(mMovieDataCursor.getString(8) != null) {
                reviews.setText("By " + mMovieDataCursor.getString(8) + "\n\n" + mMovieDataCursor.getString(9));
            } else {
                reviews.setText("No reviews available");
            }
        } else if(!mMovieTrailerReviewDataList.isEmpty() && mMovieTrailerReviewDataList.get(1) != null) {
            reviews.setText("By " + mMovieTrailerReviewDataList.get(1) + "\n\n" + mMovieTrailerReviewDataList.get(2));
        } else {
            reviews.setText("No reviews available");
        }
    }

    // Without this override, the back button will simply go back to the last activity in the stack
    // without updating said activity. If the last activity in the stack was in sorted by favorites
    // mode, it is entirely possible to remove a movie from favorites and have the movie still be
    // there because the activity doesn't update. This override ensures that the previous activity
    // gets launched again.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // If the mark as favorite button gets checked, go ahead and all the movie data into the database
    // and create a file for the movie poster. If it gets unchecked, go ahead and delete the file,
    // and then delete the entry.
    public void onFavoriteClicked(View view){
        CheckBox favoriteCheckbox = (CheckBox) findViewById(R.id.checkbox_mark_as_favorite);
        ContentResolver resolver = getContentResolver();
        Intent movieDetailActivity = getIntent();

        if(favoriteCheckbox.isChecked()){
            ContentValues contentValues = new ContentValues();
            Bitmap b = Bitmap.createBitmap(mMovieThumbnail.getWidth(), mMovieThumbnail.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            mMovieThumbnail.draw(canvas);
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
            contentValues.put(MovieContract.MovieTable.COLUMN_TRAILER,
                    mMovieTrailerReviewDataList.get(0));
            contentValues.put(MovieContract.MovieTable.COLUMN_REVIEW_AUTHOR,
                    mMovieTrailerReviewDataList.get(1));
            contentValues.put(MovieContract.MovieTable.COLUMN_REVIEW,
                    mMovieTrailerReviewDataList.get(2));
            resolver.insert(MovieContract.MovieTable.CONTENT_URI, contentValues);
        } else {
            mMovieDataCursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                    MovieContract.MovieTable.projection,
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)},
                    null);
            mMovieDataCursor.moveToFirst();
            File file = new File(mMovieDataCursor.getString(3));
            file.delete();
            resolver.delete(MovieContract.MovieTable.CONTENT_URI,
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " = ? ",
                    new String[]{movieDetailActivity.getStringExtra(MOVIE_ID)});
        }

    }

    // If the movie is in the database, go ahead and pull the link from the database. Otherwise,
    // check the array list is not empty and get the data from there instead. In the rare case,
    // that no trailers are available, handle that gracefully.
    public void onYoutubeClicked(View view) {
        if (mIsFavorite) {
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + mMovieDataCursor.getString(7)));
            startActivity(youtubeIntent);
        } else if (!mMovieTrailerReviewDataList.isEmpty()) {
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + mMovieTrailerReviewDataList.get(0)));
            startActivity(youtubeIntent);
        } else {
            Toast.makeText(this, "No trailers available", Toast.LENGTH_LONG).show();
        }

    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // If the movie was found in the database, obtain the link from the DB. Otherwise the
        // activity will make an API call and then the link will be found in the resulting array list
        // In the rare event that the movie does not have a trailer, tell the user that there was
        // no trailer gracefully.
        if (mIsFavorite){
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://www.youtube.com/watch?v=" + mMovieDataCursor.getString(7));
        } else if(!mMovieTrailerReviewDataList.isEmpty() && mMovieTrailerReviewDataList.get(0) != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://www.youtube.com/watch?v=" + mMovieTrailerReviewDataList.get(0));
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, "No trailers available :(");
        }

        return shareIntent;
    }

    // The logic used to store a movie when the "mark as favorite" checkbox is pressed saves the
    // movie poster as an actual file using the movie name as the file name. Sometimes these names
    // will contain invalid characters, this function returns a valid name.
    private String getValidFileName(String invalidName){
        String validName = invalidName.replaceAll(":","");
        return validName;
    }


    // Fetches review data & trailer data only
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

            if (movieTrailerReviewData != null){
                mMovieTrailerReviewDataList.clear();
                mMovieTrailerReviewDataList.addAll(Arrays.asList(movieTrailerReviewData));
            }
            TextView reviews = (TextView) findViewById(R.id.textview_review);

            // Update these views now with the freshly downloaded data
            if(mMovieTrailerReviewDataList.get(1) != null) {
                reviews.setText("By " + mMovieTrailerReviewDataList.get(1) + "\n\n" + mMovieTrailerReviewDataList.get(2));
            } else {
                reviews.setText("No reviews available");
            }

            // Update the Share Action Provider now with the freshly obtained data
            mShareActionProvider.setShareIntent(createShareTrailerIntent());

        }

        private String[] getMovieDataFromJSONStr(String JSONRawData) throws JSONException {

            JSONObject movieDetailJObj;
            String[] extractedMovieTrailerData =  new String[3];

            if (JSONRawData != null) {
                //Turns raw string data into a JSON object
                movieDetailJObj = new JSONObject(JSONRawData);
            } else {
                return null;
            }
            //pulls resuts array

            JSONArray movieTrailerData = movieDetailJObj.getJSONObject("trailers").getJSONArray("youtube");
            if (!movieTrailerData.isNull(0)) {
                extractedMovieTrailerData[0] = movieTrailerData.getJSONObject(0).getString("source");
            } else {
                extractedMovieTrailerData[0] = null;
            }
            JSONArray movieReviewData = movieDetailJObj.getJSONObject("reviews").getJSONArray("results");
            if (!movieReviewData.isNull(0)) {
                extractedMovieTrailerData[1] = movieReviewData.getJSONObject(0).getString("author");
                extractedMovieTrailerData[2] = movieReviewData.getJSONObject(0).getString("content");
            } else {
                extractedMovieTrailerData[1] = null;
                extractedMovieTrailerData[2] = null;
            }

            //pulls poster paths and stores them in an array
            return extractedMovieTrailerData;
        }
    }

}
