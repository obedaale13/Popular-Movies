package danielkaparunakis.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by DanielKaparunakis on 3/15/16.
 */
public class MovieContentProvider extends ContentProvider {

    private static final int MOVIE_LIST = 1;
    private static final int MOVIE_ID = 2;
    private static final UriMatcher URI_MATCHER;
    private SQLiteOpenHelper mHelper = null;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIE_LIST);
        URI_MATCHER.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHelper = MovieDatabaseHelper.getInstance(ApplicationContextUtility.getAppContext());
            }
        }, 500);
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case MOVIE_LIST:
                return MovieContract.MovieTable.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieTable.CONTENT_ITEM_TYPE;
            default:
                return null;
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)){
            case MOVIE_LIST:
                updateCount = db.update(MovieContract.MovieTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_ID:
                String idStr = uri.getLastPathSegment();
                String where = MovieContract.MovieTable._ID + " = " + idStr;
                if(!TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }
                updateCount = db.update(MovieContract.MovieTable.TABLE_NAME, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(URI_MATCHER.match(uri) != MOVIE_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if(URI_MATCHER.match(uri) == MOVIE_LIST) {
            long id = db.insert(MovieContract.MovieTable.TABLE_NAME, null, values);
            return getUriforID(id,uri);
        } else {
            return null;
        }
    }

    private Uri getUriforID(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri,id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int delCount = 0;
        switch (URI_MATCHER.match(uri)){
            case MOVIE_LIST:
                delCount = db.delete(MovieContract.MovieTable.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                String idStr = uri.getLastPathSegment();
                String where = MovieContract.MovieTable._ID + " = " + idStr;
                if(!TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }
                delCount = db.delete(MovieContract.MovieTable.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case MOVIE_LIST:
                builder.setTables(MovieContract.MovieTable.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = MovieContract.MovieTable.SORT_ORDER_DEFAULT;
                }
                break;
            case MOVIE_ID:
                builder.setTables(MovieContract.MovieTable.TABLE_NAME);
                builder.appendWhere(MovieContract.MovieTable._ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri);
        return cursor;
    }
}
