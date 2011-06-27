package asia.ivity.android.tiledscrollview;

import android.app.Application;
import android.content.Context;

/**
 * TiledScrollView Demo application
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class TiledScrollViewDemo extends Application {

    public static Context getRealApplicationContext() {
        return mApplicationContext;
    }

    static Context mApplicationContext;


    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationContext = getApplicationContext();
    }
}
