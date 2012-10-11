package asia.ivity.android.tiledscrollview;

import android.app.Activity;
import android.os.Bundle;

public class TiledScrollViewSample extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final TiledScrollView tiledScrollView = (TiledScrollView) findViewById(R.id.tiledScrollView);

        tiledScrollView.addConfigurationSet(TiledScrollView.ZoomLevel.LEVEL_1,
                new ConfigurationSet("tiger800/crop_%col%_%row%.png", 100, 100, 800, 600));
        tiledScrollView.addConfigurationSet(TiledScrollView.ZoomLevel.LEVEL_2,
                new ConfigurationSet("tiger1600/crop_%col%_%row%.png", 100, 100, 1600, 1200));
    }
}
