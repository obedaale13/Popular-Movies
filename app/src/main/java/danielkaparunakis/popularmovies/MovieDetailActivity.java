package danielkaparunakis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Enclosing class for the detail fragment
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
