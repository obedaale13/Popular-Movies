package danielkaparunakis.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DanielKaparunakis on 12/17/15.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mMoviePosterPaths = new ArrayList<String>();
    private final String POSTER_FULL_PATH = "http://image.tmdb.org/t/p/w185/";

    public ImageAdapter (Context context, List<String> moviePosterPaths) {
        mContext = context;
        Collections.copy(moviePosterPaths, mMoviePosterPaths);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return 5;
    }

    public Object getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(185, 277));
        } else {
            imageView = (ImageView) convertView;
        }
        //Code will not compile with this line. Says the ArrayList is empty
//          Picasso.with(mContext).load(POSTER_FULL_PATH + mMoviePosterPaths.get(position).toString()).into(imageView);
        return imageView;

    }
}
