package danielkaparunakis.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DanielKaparunakis on 3/15/16.
 */
public class MovieDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites.db";
    private static final int SCHEMA_VERSION = 2;
    private static MovieDatabaseHelper singleton = null;

    synchronized static MovieDatabaseHelper getInstance(Context context){
        if (singleton == null){
            singleton = new MovieDatabaseHelper(context.getApplicationContext());
        }

        return singleton;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        upgradeMyDatabase(db, 0, SCHEMA_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeMyDatabase(db, oldVersion, newVersion);
    }

    private MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    private void upgradeMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion < 1){
            db.execSQL("CREATE TABLE " + MovieContract.MovieTable.TABLE_NAME + " (" +
                    MovieContract.MovieTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MovieContract.MovieTable.COLUMN_MOVIE_ID + " INTEGER UNIQUE," +
                    MovieContract.MovieTable.COLUMN_ORIGINAL_TITLE + " TEXT," +
                    MovieContract.MovieTable.COLUMN_POSTER_PATH + " TEXT," +
                    MovieContract.MovieTable.COLUMN_OVERVIEW + " TEXT," +
                    MovieContract.MovieTable.COLUMN_VOTE_AVERAGE + " REAL," +
                    MovieContract.MovieTable.COLUMN_RELEASE_DATE + " TEXT" +
                    ");");
        }

        if(oldVersion < 2){
            db.execSQL("ALTER TABLE " + MovieContract.MovieTable.TABLE_NAME + " ADD COLUMN " +
                    MovieContract.MovieTable.COLUMN_TRAILER + " TEXT;");
            db.execSQL("ALTER TABLE " + MovieContract.MovieTable.TABLE_NAME + " ADD COLUMN " +
                    MovieContract.MovieTable.COLUMN_REVIEW_AUTHOR + " TEXT;");
            db.execSQL("ALTER TABLE " + MovieContract.MovieTable.TABLE_NAME + " ADD COLUMN " +
                    MovieContract.MovieTable.COLUMN_REVIEW + " TEXT;");
            db.execSQL("ALTER TABLE " + MovieContract.MovieTable.TABLE_NAME + " ADD COLUMN " +
                    MovieContract.MovieTable.COLUMN_FAVORITE + " NUMERIC;");
        }

    }


}
