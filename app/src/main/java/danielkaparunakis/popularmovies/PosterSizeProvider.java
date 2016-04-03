package danielkaparunakis.popularmovies;

/**
 * Created by DanielKaparunakis on 4/3/16.
 * Utility class designed to provide various resolutions for the movie posters based on the density
 * of the device the app is running on.
 */
public class PosterSizeProvider {

    public static int getPosterWidth(float density){
        if (density == 2.0){
            return 342;
        } else if (density == 3.0){
            return 500;
        } else if (density == 4.0){
            return 780;
        } else {
            return 342;
        }
    }

    public static int getPosterHeight(float density){
        if (density == 2.0){
            return 513;
        } else if (density == 3.0){
            return 750;
        } else if (density == 4.0){
            return 1170;
        } else {
            return 513;
        }
    }

}
