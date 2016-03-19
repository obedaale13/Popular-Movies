package danielkaparunakis.popularmovies;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by DanielKaparunakis on 3/15/16.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "danielkaparunakis.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

    public static final class MovieTable implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIE);
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "favoriteMovies";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_ORIGINAL_TITLE = "original_title";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_TRAILER = "trailers";

        public static final String COLUMN_REVIEW_AUTHOR = "author";

        public static final String COLUMN_REVIEW = "reviews";

        public static final String COLUMN_FAVORITE = "favorite";

        public static final String SORT_ORDER_DEFAULT = COLUMN_MOVIE_ID + " ASC";

        public static final String[] projection = new String[]{
                MovieTable._ID,
                COLUMN_MOVIE_ID,
                COLUMN_ORIGINAL_TITLE,
                COLUMN_POSTER_PATH,
                COLUMN_OVERVIEW,
                COLUMN_VOTE_AVERAGE,
                COLUMN_RELEASE_DATE,
                COLUMN_TRAILER,
                COLUMN_REVIEW_AUTHOR,
                COLUMN_REVIEW,
                COLUMN_FAVORITE
        };
    }
}
