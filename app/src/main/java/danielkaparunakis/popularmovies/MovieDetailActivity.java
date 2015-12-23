package danielkaparunakis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class MovieDetailActivity extends AppCompatActivity {

    private final String ORIGINAL_TITLE   = "original_title";
    private final String POSTER_PATH      = "poster_path";
    private final String OVERVIEW         = "overview";
    private final String VOTE_AVERAGE     = "vote_average";
    private final String RELEASE_DATE     = "release_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent movieDetailActivity = getIntent();
        Log.v("Data", movieDetailActivity.getStringExtra(ORIGINAL_TITLE));
        Log.v("Data", movieDetailActivity.getStringExtra(POSTER_PATH));
        Log.v("Data", movieDetailActivity.getStringExtra(OVERVIEW));
        Log.v("Data", movieDetailActivity.getStringExtra(VOTE_AVERAGE));
        Log.v("Data", movieDetailActivity.getStringExtra(RELEASE_DATE));
    }

}
