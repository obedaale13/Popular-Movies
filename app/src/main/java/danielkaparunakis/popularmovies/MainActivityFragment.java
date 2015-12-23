package danielkaparunakis.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    private final String DEFAULT_API_CALL = "now_playing";
    private final String POPULARITY       = "popular";
    private final String TOP_RATED        = "top_rated";
    private final String ORIGINAL_TITLE   = "original_title";
    private final String POSTER_PATH      = "poster_path";
    private final String OVERVIEW         = "overview";
    private final String VOTE_AVERAGE     = "vote_average";
    private final String RELEASE_DATE     = "release_date";
    private String currentSortSetting     = null;
    List<String> mMoviePosterPaths        = new ArrayList<String>();
    ImageAdapter imageAdapter;
    GridView moviePosterGrid;
    JSONArray movieDataArray;

    public MainActivityFragment() {
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
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Setting used to determine the sort order of the movies
        switch (item.getItemId()) {
            case R.id.action_sort_by_popularity:
                new FetchMovieDataTask().execute(POPULARITY);
                return true;
            case R.id.action_sort_by_highest_rated:
                new FetchMovieDataTask().execute(TOP_RATED);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate fragment when View is created, then stored to be returned at the end of the method
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Fetch movie data using API
        new FetchMovieDataTask().execute(DEFAULT_API_CALL);

        //Find the Movie Posters Grid by ID & bind it to the custom-made ImageView adapter
        moviePosterGrid = (GridView) rootView.findViewById(R.id.movie_poster_grid);
        moviePosterGrid.setColumnWidth(500);
        moviePosterGrid.setNumColumns(2);
        imageAdapter = new ImageAdapter(getActivity());
        moviePosterGrid.setAdapter(imageAdapter);
        moviePosterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent movieDetailActivity = new Intent(getActivity(), MovieDetailActivity.class);
                try{
                    movieDetailActivity.putExtra(ORIGINAL_TITLE, movieDataArray.getJSONObject(position).getString("original_title"));
                    movieDetailActivity.putExtra(POSTER_PATH, movieDataArray.getJSONObject(position).getString("poster_path"));
                    movieDetailActivity.putExtra(OVERVIEW, movieDataArray.getJSONObject(position).getString("overview"));
                    movieDetailActivity.putExtra(VOTE_AVERAGE, movieDataArray.getJSONObject(position).getString("vote_average"));
                    movieDetailActivity.putExtra(RELEASE_DATE, movieDataArray.getJSONObject(position).getString("release_date"));
                } catch (Exception e) {
                    Log.e("Error", "JSONArray was null");
                }
                startActivity(movieDetailActivity);
            }
        });

        return rootView;
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
            mMoviePosterPaths.clear();
            mMoviePosterPaths.addAll(Arrays.asList(MoviePosterPaths));
            imageAdapter.setmMoviePosterPaths(mMoviePosterPaths);
            imageAdapter.notifyDataSetInvalidated();
        }

        private String[] getMovieDataFromJSONStr(String JSONRawData) throws JSONException {

            //Turns raw string data into a JSON object
            JSONObject movieDataJSONobj = new JSONObject(JSONRawData);

            //pulls resuts array
            movieDataArray = movieDataJSONobj.getJSONArray("results");

            //pulls poster paths and stores them in an array
            String[] moviePosterPaths = new String[movieDataArray.length()];
            for (int i = 0; i < movieDataArray.length(); i++) {
                moviePosterPaths[i] = movieDataArray.getJSONObject(i).getString("poster_path");
            }
            return moviePosterPaths;

        }
    }



}
