package danielkaparunakis.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DanielKaparunakis on 12/17/15.
 */
public class ImageAdapter extends BaseAdapter {

    //Member fields
    private Context mContext;
    private List<String> mMoviePosterPaths = new ArrayList<String>();
    private boolean mIsLocal = false;

    //Contructor
    public ImageAdapter (Context context) {
        mContext = context;
    }

    //Necessary override, not used
    @Override
    public long getItemId(int position) {
        return 0;
    }

    //Override used to get count of total views to display
    @Override
    public int getCount() {
        return mMoviePosterPaths.size();
    }

    //Method used to update data in MoviePosterPath array
    public void setmMoviePosterPaths (List<String> MoviePosterPaths) {
        mMoviePosterPaths = MoviePosterPaths;
    }

    public void setLocalFileFlag(boolean isLocal) {
        mIsLocal = isLocal;
    }

    public Object getItem(int position) {
        return null;
    }

    //Generate view
    public View getView(int position, View convertView, ViewGroup parent) {

        //ImageView parameters
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(500, 750));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        //Use of Picasso library to load, cache & display images, only works if the path is not null
        //which the API returns sometimes
        final String POSTER_FULL_PATH = "http://image.tmdb.org/t/p/w500";
        if (!mMoviePosterPaths.get(position).equals(null)){
            if (mIsLocal){
                Picasso.with(mContext)
                        .load(new File(mMoviePosterPaths.get(position)))
                        .into(imageView);
            } else {
                Picasso.with(mContext)
                        .load(POSTER_FULL_PATH + mMoviePosterPaths.get(position))
                        .into(imageView);
            }
        }

        return imageView;

    }
}
