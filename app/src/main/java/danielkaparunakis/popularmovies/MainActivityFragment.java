package danielkaparunakis.popularmovies;

import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String DEFAULT_API_CALL            = "now_playing";
    private final String POPULARITY                  = "popular";
    private final String TOP_RATED                   = "top_rated";
    private final String MOVIE_ID                    = "id";
    private final String ORIGINAL_TITLE              = "original_title";
    private final String POSTER_PATH                 = "poster_path";
    private final String OVERVIEW                    = "overview";
    private final String VOTE_AVERAGE                = "vote_average";
    private final String RELEASE_DATE                = "release_date";
    private final String SAVED_INSTANCE_POSTER_PATHS = "mMoviePosterPaths";
    private final String SAVED_INSTANCE_JSON_RAW     = "JSONRawData";
    public static final String SORTED_BY_FAVORITE    = "mSortedByFavorite";
    private final String LOG_TAG                     = MainActivityFragment.class.getSimpleName();

    private boolean mSortedByFavorite                = false;
    private ArrayList<String> mMoviePosterPaths      = new ArrayList<String>();
    private Cursor mCursor;
    private ImageAdapter mImageAdapter;
    private JSONArray mMovieDataArray;
    private JSONObject mMovieDataJSONObj;

    public MainActivityFragment() {
    }

    // The mMoviePosterPaths serves to display all the poster in the main activity after a configuration
    // change without an additional API call. The JSON object contains movie data passed in the detailActivity
    // Intent so it is also necessary to save it
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mMoviePosterPaths != null){
            outState.putStringArrayList(SAVED_INSTANCE_POSTER_PATHS, mMoviePosterPaths);
        }
        if (mMovieDataJSONObj != null){
            outState.putString(SAVED_INSTANCE_JSON_RAW, mMovieDataJSONObj.toString());
        }
        outState.putBoolean(SORTED_BY_FAVORITE, mSortedByFavorite);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu located in main
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Setting used to determine the sort order of the movies
        switch (item.getItemId()) {
            case R.id.action_sort_by_popularity:
                mImageAdapter.setLocalFileFlag(false);
                new FetchMovieDataTask().execute(POPULARITY);
                mSortedByFavorite = false;
                return true;
            case R.id.action_sort_by_highest_rated:
                mImageAdapter.setLocalFileFlag(false);
                new FetchMovieDataTask().execute(TOP_RATED);
                mSortedByFavorite = false;
                return true;
            case R.id.action_sort_by_favorites_movies:
                mImageAdapter.setLocalFileFlag(true);
                favoriteMovies();
                mSortedByFavorite = true;
                mImageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
                mImageAdapter.notifyDataSetInvalidated();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        //Inflate fragment when View is created, then stored to be returned at the end of the method
        final View rootView = inflater.inflate(R.layout.gridview, container, false);
        //Find the Movie Posters Grid by ID & bind it to the custom-made ImageView adapter
        GridView MoviePosterGrid  = (GridView) rootView.findViewById(R.id.movie_poster_grid);
        MoviePosterGrid.setColumnWidth(500);
        if(Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
            MoviePosterGrid.setNumColumns(2);
        } else {
            MoviePosterGrid.setNumColumns(4);
        }
        mImageAdapter = new ImageAdapter(getActivity());

        // If there is data in savedInstance, pull from there, otherwise if the app is online,
        // make an API call. If we are offline, use the poster paths stored in the database
        if (savedInstanceState != null) {
            //Populate array with previous instance's data
            mMoviePosterPaths = savedInstanceState.getStringArrayList(SAVED_INSTANCE_POSTER_PATHS);

            //Populate the JSON Object with previous instance's data in case of detail activity launch

            if (savedInstanceState.getString(SAVED_INSTANCE_JSON_RAW) != null && !savedInstanceState.getBoolean(SORTED_BY_FAVORITE)){
                try {
                    mMovieDataJSONObj =
                            new JSONObject(savedInstanceState.getString(SAVED_INSTANCE_JSON_RAW));
                    mMovieDataArray = mMovieDataJSONObj.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                //Reset ImageAdapter
                mImageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
                mImageAdapter.notifyDataSetInvalidated();
            } else {
                mImageAdapter.setLocalFileFlag(true);
                mSortedByFavorite = true;
                //Reset ImageAdapter
                mImageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
                mImageAdapter.notifyDataSetInvalidated();
            }

        } else if (ConnectivityStatus.isOnline()){
            //Launch default API call
            new FetchMovieDataTask().execute(DEFAULT_API_CALL);
            mSortedByFavorite = false;
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageAdapter.setLocalFileFlag(true);
                    favoriteMovies();
                    mSortedByFavorite = true;
                    mImageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
                    mImageAdapter.notifyDataSetInvalidated();
                }
            }, 550);

        }
        MoviePosterGrid.setAdapter(mImageAdapter);

        // All send you to the detailActivity. In the first case, since the movies are being pulled
        // from the database and savedInstanceState does not have anything, the activity sends all data
        // from the database. In the second, the mCursor may not longer have data since savedInstanceState
        // now has data, therefore, the mCursor gets recreated and then sent off.
        MoviePosterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
                if (fragmentContainer != null) {
                    MovieDetailActivityFragment movieDetailActivityFragment = new MovieDetailActivityFragment();
                    Bundle bundle = new Bundle();

                    if(mSortedByFavorite && savedInstanceState == null){
                        mCursor.moveToPosition(position);
                        bundle.putString(MOVIE_ID, mCursor.getString(1));
                    } else if(mSortedByFavorite && savedInstanceState != null){
                        ContentResolver resolver = getActivity().getContentResolver();
                        mCursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                                new String[]{MovieContract.MovieTable._ID, MovieContract.MovieTable.COLUMN_MOVIE_ID
                                        , MovieContract.MovieTable.COLUMN_POSTER_PATH},
                                MovieContract.MovieTable.COLUMN_POSTER_PATH + " = ?",
                                new String[]{mMoviePosterPaths.get(position)},
                                null);
                        mCursor.moveToFirst();
                        bundle.putString(MOVIE_ID, mCursor.getString(1));
                    } else {
                        //Use data currently residing in the JSONArray instead of querying the server again
                        try {
                            bundle.putString(MOVIE_ID, mMovieDataArray.getJSONObject(position).getString("id"));
                            bundle.putString(ORIGINAL_TITLE, mMovieDataArray.getJSONObject(position).getString("original_title"));
                            bundle.putString(POSTER_PATH, mMovieDataArray.getJSONObject(position).getString("poster_path"));
                            bundle.putString(OVERVIEW, mMovieDataArray.getJSONObject(position).getString("overview"));
                            bundle.putString(VOTE_AVERAGE, mMovieDataArray.getJSONObject(position).getString("vote_average"));
                            bundle.putString(RELEASE_DATE, mMovieDataArray.getJSONObject(position).getString("release_date"));
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                    movieDetailActivityFragment.setArguments(bundle);
                    FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, movieDetailActivityFragment);
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                } else {
                    Intent movieDetailActivity = new Intent(getActivity(), MovieDetailActivity.class);

                    if(mSortedByFavorite && savedInstanceState == null){
                        mCursor.moveToPosition(position);
                        movieDetailActivity.putExtra(MOVIE_ID, mCursor.getString(1));
                    } else if(mSortedByFavorite && savedInstanceState != null){
                        ContentResolver resolver = getActivity().getContentResolver();
                        mCursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                                new String[]{MovieContract.MovieTable._ID, MovieContract.MovieTable.COLUMN_MOVIE_ID
                                        , MovieContract.MovieTable.COLUMN_POSTER_PATH},
                                MovieContract.MovieTable.COLUMN_POSTER_PATH + " = ?",
                                new String[]{mMoviePosterPaths.get(position)},
                                null);
                        mCursor.moveToFirst();
                        movieDetailActivity.putExtra(MOVIE_ID, mCursor.getString(1));
                    } else {
                        //Use data currently residing in the JSONArray instead of querying the server again
                        try {
                            movieDetailActivity.putExtra(MOVIE_ID, mMovieDataArray.getJSONObject(position).getString("id"));
                            movieDetailActivity.putExtra(ORIGINAL_TITLE, mMovieDataArray.getJSONObject(position).getString("original_title"));
                            movieDetailActivity.putExtra(POSTER_PATH, mMovieDataArray.getJSONObject(position).getString("poster_path"));
                            movieDetailActivity.putExtra(OVERVIEW, mMovieDataArray.getJSONObject(position).getString("overview"));
                            movieDetailActivity.putExtra(VOTE_AVERAGE, mMovieDataArray.getJSONObject(position).getString("vote_average"));
                            movieDetailActivity.putExtra(RELEASE_DATE, mMovieDataArray.getJSONObject(position).getString("release_date"));
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }

                    startActivity(movieDetailActivity);
                }


            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCursor != null) {
            mCursor.close();
        }
    }

    // This method fetches all the poster paths for movies stored in the database
    private void favoriteMovies(){
        ContentResolver resolver = getActivity().getContentResolver();

        mCursor = resolver.query(MovieContract.MovieTable.CONTENT_URI,
                new String[]{MovieContract.MovieTable._ID, MovieContract.MovieTable.COLUMN_MOVIE_ID, MovieContract.MovieTable.COLUMN_POSTER_PATH},
                null,
                null,
                null);
        String[] moviePosterPaths = new String[mCursor.getCount()];
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToNext();
            moviePosterPaths[i] = mCursor.getString(2);
        }
        mMoviePosterPaths.clear();
        mMoviePosterPaths.addAll(Arrays.asList(moviePosterPaths));
    }

    public class FetchMovieDataTask extends AsyncTask<String, Void, String[]> {

        private final String BUILDER_SCHEME = "http";
        private final String BUILDER_AUTHORITY = "api.themoviedb.org";
        private final String BUILDER_PATH_1 = "3";
        private final String BUILDER_PATH_2 = "movie";
        private final String APIKEY_PARAM = "api_key";
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

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
                    .appendQueryParameter(APIKEY_PARAM, ""); //Your API KEY goes here

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
        protected void onPostExecute(String[] MoviePosterPaths) {
            super.onPostExecute(MoviePosterPaths);

            //Take data received from the execution of Asynctask and update an ArrayList with it
            //Then update the image adapter
            if (MoviePosterPaths != null){
                mMoviePosterPaths.clear();
                mMoviePosterPaths.addAll(Arrays.asList(MoviePosterPaths));
                mImageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
                mImageAdapter.notifyDataSetInvalidated();
            }
        }

        private String[] getMovieDataFromJSONStr(String JSONRawData) throws JSONException {

            if (JSONRawData != null) {
                //Turns raw string data into a JSON object
                mMovieDataJSONObj = new JSONObject(JSONRawData);
            } else {
                return null;
            }

            //pulls resuts array
            mMovieDataArray = mMovieDataJSONObj.getJSONArray("results");

            //pulls poster paths and stores them in an array
            String[] moviePosterPaths = new String[mMovieDataArray.length()];
            for (int i = 0; i < mMovieDataArray.length(); i++) {
                moviePosterPaths[i] = mMovieDataArray.getJSONObject(i).getString("poster_path");
            }
            return moviePosterPaths;

        }
    }



}
