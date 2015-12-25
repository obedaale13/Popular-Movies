package danielkaparunakis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity {

    private final String ORIGINAL_TITLE   = "original_title";
    private final String POSTER_PATH      = "poster_path";
    private final String OVERVIEW         = "overview";
    private final String VOTE_AVERAGE     = "vote_average";
    private final String RELEASE_DATE     = "release_date";
    private final String POSTER_FULL_PATH = "http://image.tmdb.org/t/p/w500";
    private final String MAX_VOTE_AVERAGE = "/10";
    private final String LOG_TAG          = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent movieDetailActivity = getIntent();

        getSupportActionBar().setTitle(movieDetailActivity.getStringExtra(ORIGINAL_TITLE));

        ImageView movieThumbnail = (ImageView) findViewById(R.id.movie_thumbnail);
        movieThumbnail.setMaxHeight(750);
        movieThumbnail.setMaxWidth(500);
        movieThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(this).load(POSTER_FULL_PATH + movieDetailActivity.getStringExtra(POSTER_PATH)).into(movieThumbnail);

        TextView overview = (TextView) findViewById(R.id.overview);
        overview.setText(movieDetailActivity.getStringExtra(OVERVIEW));

        String newReleaseDate = null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(movieDetailActivity.getStringExtra((RELEASE_DATE)));
            newReleaseDate = new SimpleDateFormat("yyyy").format(date);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Parse error");
        }

        TextView releaseDate = (TextView) findViewById(R.id.release_date);
        releaseDate.setText(newReleaseDate);

        TextView voteAverage = (TextView) findViewById(R.id.vote_average);
        voteAverage.setText(movieDetailActivity.getStringExtra(VOTE_AVERAGE) + MAX_VOTE_AVERAGE);

    }

}
