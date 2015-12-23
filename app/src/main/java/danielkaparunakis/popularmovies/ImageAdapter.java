package danielkaparunakis.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DanielKaparunakis on 12/17/15.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mMoviePosterPaths = new ArrayList<String>();
    private final String POSTER_FULL_PATH = "http://image.tmdb.org/t/p/w500";

    public ImageAdapter (Context context) {
        mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setmMoviePosterPaths (List<String> MoviePosterPaths) {
        mMoviePosterPaths = MoviePosterPaths;
    }

    @Override
    public int getCount() {
        return mMoviePosterPaths.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(500, 750));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        //Code will not compile with this line. Says the ArrayList is empty
        try {
            Picasso.with(mContext).load(POSTER_FULL_PATH + mMoviePosterPaths.get(position).toString()).into(imageView);
        } catch (Exception e) {
            Log.v("Fawk man", "");
        }
        return imageView;

    }
}
