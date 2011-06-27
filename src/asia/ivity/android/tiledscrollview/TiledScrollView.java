package asia.ivity.android.tiledscrollview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiled Scroll View. Similar to iOS's CATiledLayer.
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class TiledScrollView extends TwoDScrollView {
    static final int WIDTH = 1500;
    static final int HEIGHT = 1520;

    static final int UPDATE_TILES = 123;
    static final int CLEANUP_OLD_TILES = 124;


    private int[] demoColors = new int[]{Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE};

    private Animation fadeIn = AnimationUtils.loadAnimation(
            TiledScrollViewDemo.getRealApplicationContext(), R.anim.fadein);

    private FrameLayout mContainer;
    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final String TAG = TiledScrollView.class.getSimpleName();
    private float mDensity;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case UPDATE_TILES:
                    fillTiles();
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, CLEANUP_OLD_TILES), 1000);
                    break;
                case CLEANUP_OLD_TILES:
                    Log.d(TAG, "Cleanup!");
                    cleanupOldTiles();
                    break;
            }

        }
    };


    private Map<Tile, WeakReference<ImageView>> tiles
            = new ConcurrentHashMap<Tile, WeakReference<ImageView>>();

    public TiledScrollView(Context context) {
        super(context);

        init();
    }

    public TiledScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TiledScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        mContainer = new FrameLayout(getContext());

        int rows = (int) Math.ceil(WIDTH / TILE_WIDTH);
        int cols = (int) Math.ceil(HEIGHT / TILE_HEIGHT);

        final LayoutParams lp = new LayoutParams(WIDTH, HEIGHT);
        mContainer.setMinimumWidth(WIDTH);
        mContainer.setMinimumHeight(HEIGHT);
        mContainer.setLayoutParams(lp);

        addView(mContainer, lp);

        mContainer.setBackgroundColor(android.R.color.white);

        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fillTiles();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        Message msg = Message.obtain();
        msg.what = UPDATE_TILES;

        if (mHandler.hasMessages(UPDATE_TILES)) {
            mHandler.removeMessages(UPDATE_TILES);
        }

        mHandler.sendMessageDelayed(msg, 200);
    }

    private void fillTiles() {
        Rect visible = new Rect();
        mContainer.getDrawingRect(visible);


        int left = visible.left + getScrollX();
        int top = visible.top + getScrollY();
        int width = (int) (getMeasuredWidth() * mDensity) + getScrollX();
        int height = (int) (getMeasuredHeight() * mDensity) + getScrollY();

        Log.d(TAG, "Top    : " + top);
        Log.d(TAG, "Left   : " + left);
        Log.d(TAG, "Width  : " + width);
        Log.d(TAG, "Height : " + height);

        for (int y = top; y < height; ) {
            int tileY = new Double(Math.ceil(y / TILE_HEIGHT)).intValue();
            for (int x = left; x < width; ) {
                int tileX = new Double(Math.ceil(x / TILE_WIDTH)).intValue();

                final Tile tile = new Tile(tileX, tileY);

                if (!tiles.containsKey(tile) || tiles.get(tile).get() == null) {
                    final ImageView iv = getNewTile(tileX, tileY);
                    iv.setId(new Random().nextInt());
                    FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(TILE_WIDTH, TILE_HEIGHT);
                    lp2.leftMargin = tileX * TILE_WIDTH;
                    lp2.topMargin = tileY * TILE_HEIGHT;
                    lp2.gravity = Gravity.TOP | Gravity.LEFT;
                    iv.setLayoutParams(lp2);

                    mContainer.addView(iv, lp2);
                    tiles.put(tile, new WeakReference<ImageView>(iv));

//                Message msg = Message.obtain();
//                msg.what = 111;
//                msg.arg1 = iv.getId();

//                mHandler.sendMessage(msg);
                }

                x = x + TILE_WIDTH;
            }
            y = y + TILE_HEIGHT;
        }
    }

    private ImageView getNewTile(int x, int y) {
        ImageView iv = new ImageView(getContext());

        iv.setImageResource(getResources().getIdentifier("crop_" + y + "_" + x, "drawable",
                TiledScrollViewDemo.class.getPackage().getName()));
        iv.setMinimumHeight(TILE_HEIGHT);
        iv.setMinimumWidth(TILE_WIDTH);

        return iv;
    }

    private void cleanupOldTiles() {
        for (Tile tile : tiles.keySet()) {
            ImageView v = tiles.get(tile).get();
            Rect r = new Rect();

            v.getHitRect(r);

            Rect actualRect = new Rect(
                    getScrollX(), getScrollY(),
                    getWidth() + getScrollX(),
                    getHeight() + getScrollY()
            );

            if(!Rect.intersects(actualRect, r)) {
                mContainer.removeView(v);
            }
            tiles.remove(tile);
        }
    }

    private class Tile extends Pair<Integer, Integer> {
        /**
         * Constructor for a Pair. If either are null then equals() and hashCode() will throw
         * a NullPointerException.
         *
         * @param first  the first object in the Pair
         * @param second the second object in the pair
         */
        public Tile(Integer first, Integer second) {
            super(first, second);
        }
    }
}
