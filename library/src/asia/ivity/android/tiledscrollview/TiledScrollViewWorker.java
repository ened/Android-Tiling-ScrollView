/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * (c) 2011 Sebastian Roth <sebastian.roth@gmail.com>
 */

package asia.ivity.android.tiledscrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiled Scroll View worker class that handles loading and display of the pictures.
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class TiledScrollViewWorker extends TwoDScrollView {
    static final int UPDATE_TILES = 123;
    //    static final int CLEANUP_OLD_TILES = 124;
    static final int FILL_TILES_DELAY = 200;

    private Animation mFadeInAnimation;
    private OnZoomLevelChangedListener onZoomLevelChangedListener = null;
    private List<Marker> mMarkers = new ArrayList<Marker>();

    private OnClickListener mOnMarkerOnClickListener;
    private List<ImageView> mMarkerViews = new ArrayList<ImageView>();

    public void setMarkerOnClickListener(OnClickListener mOnMarkerOnClickListener) {
        this.mOnMarkerOnClickListener = mOnMarkerOnClickListener;
    }

    public void setOnZoomLevelChangedListener(OnZoomLevelChangedListener listener) {
        this.onZoomLevelChangedListener = listener;
    }

    public void addMarker(int x, int y, String description) {
        mMarkers.add(new Marker(x, y, description));
    }

    TiledScrollView.ZoomLevel mCurrentZoomLevel = TiledScrollView.ZoomLevel.DEFAULT;

    Map<TiledScrollView.ZoomLevel, ConfigurationSet> mConfigurationSets = new HashMap<TiledScrollView.ZoomLevel, ConfigurationSet>();

    private ConfigurationSet getCurrentConfigurationSet() {
        if (mConfigurationSets.containsKey(mCurrentZoomLevel)) {
            return mConfigurationSets.get(mCurrentZoomLevel);
        }

        return mConfigurationSets.get(TiledScrollView.ZoomLevel.DEFAULT);
    }

    public void addConfigurationSet(TiledScrollView.ZoomLevel level, ConfigurationSet set) {
        mConfigurationSets.put(level, set);
    }

    private FrameLayout mContainer;
    private static final String TAG = TiledScrollViewWorker.class.getSimpleName();
    //    private float mDensity;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case UPDATE_TILES:
                    try {
                        fillTiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    mHandler.sendMessageDelayed(Message.obtain(mHandler, CLEANUP_OLD_TILES), 1000);
                    break;
//                case CLEANUP_OLD_TILES:
//                    cleanupOldTiles();
//                    break;
            }
        }
    };

    private Map<Tile, SoftReference<ImageView>> tiles = new ConcurrentHashMap<Tile, SoftReference<ImageView>>();

    public TiledScrollViewWorker(Context context, AttributeSet attrs) {
        super(context, attrs);

        readAttributes(attrs);

        init();
    }

    public TiledScrollViewWorker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        readAttributes(attrs);

        init();
    }

    private void readAttributes(AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView);

        int imageWidth = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_image_width, -1);
        int imageHeight = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_image_height, -1);
        int tileWidth = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_tile_width, -1);
        int tileHeight = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_tile_height, -1);
        String filePattern = a.getString(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_file_pattern);

        // TODO: Move Validation to ConfigurationSet itself.
        if (imageWidth == -1 || imageHeight == -1 || tileWidth == -1 || tileHeight == -1 || filePattern == null) {
            throw new IllegalArgumentException("Please set all attributes correctly!");
        }

        mConfigurationSets.put(TiledScrollView.ZoomLevel.DEFAULT,
                new ConfigurationSet(filePattern, tileWidth, tileHeight, imageWidth, imageHeight));
    }

    private void init() {
        mFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);

        mContainer = new ZoomingFrameLayout(getContext());

        ConfigurationSet set = getCurrentConfigurationSet();

        final LayoutParams lp = new LayoutParams(set.getImageWidth(), set.getImageHeight());

        // Required?
        mContainer.setMinimumWidth(set.getImageWidth());
        mContainer.setMinimumHeight(set.getImageHeight());
        mContainer.setLayoutParams(lp);

        addView(mContainer, lp);

        mContainer.setBackgroundColor(android.R.color.white);
//
//        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        try {
            fillTiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        Message msg = Message.obtain();
        msg.what = UPDATE_TILES;

        if (mHandler.hasMessages(UPDATE_TILES)) {
            mHandler.removeMessages(UPDATE_TILES);
        }

        mHandler.sendMessageDelayed(msg, FILL_TILES_DELAY);
    }

    private void fillTiles() throws IOException {
        Rect visible = new Rect();
        mContainer.getDrawingRect(visible);

        for (ImageView iv : mMarkerViews) {
            mContainer.removeView(iv);
        }

//        for (int i = 0; i < mContainer.getChildCount(); i++) {
//            View x = mContainer.getChildAt(i);
//            if (x.getTag() instanceof Marker) {
//                mContainer.removeView(x);
//                i--;
//            }
//        }

        final int left = visible.left + getScrollX();
        final int top = visible.top + getScrollY();

        final ConfigurationSet set = getCurrentConfigurationSet();

        // Update the logic here. Sometimes, we don't need to add 1 tile to the right and bottom,
        // as it might be already exact. In that case, it's loading tiles that will be cleaned up
        // immediately in #cleanupTiles().
        final int width = (int) (getMeasuredWidth()) + getScrollX() + set.getTileWidth();
        final int height = (int) (getMeasuredHeight()) + getScrollY() + set.getTileHeight();

//        Log.d(TAG, "Top    : " + top);
//        Log.d(TAG, "Left   : " + left);
//        Log.d(TAG, "Width  : " + width);
//        Log.d(TAG, "Height : " + height);
//
        new AsyncTask<Void, ImageView, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (int y = top; y < height; ) {
                    final int tileY = new Double(Math.ceil(y / set.getTileHeight())).intValue();
                    for (int x = left; x < width; ) {
                        final int tileX = new Double(Math.ceil(x / set.getTileWidth())).intValue();

                        final Tile tile = new Tile(tileX, tileY);

                        if (!tiles.containsKey(tile) || tiles.get(tile).get() == null) {
                            try {
                                publishProgress(getNewTile(tile));
                            } catch (IOException e) {
                                // Do nothing.
                            }
                        } else {
                        }

                        x = x + set.getTileWidth();
                    }
                    y = y + set.getTileHeight();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(ImageView... ivs) {
                for (ImageView iv : ivs) {
                    if (iv == null) {
                        continue;
                    }

                    final Tile tile = (Tile) iv.getTag();

                    iv.setId(new Random().nextInt());
                    FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                    lp2.leftMargin = tile.x * set.getTileWidth();
                    lp2.topMargin = tile.y * set.getTileHeight();
                    lp2.gravity = Gravity.TOP | Gravity.LEFT;
                    iv.setLayoutParams(lp2);

                    mContainer.addView(iv, lp2);

                    // Not yet functional.
                    // Log.d(TAG, "Animating: " + tile);
                    // iv.startAnimation(mFadeInAnimation);

                    tiles.put(tile, new SoftReference<ImageView>(iv));
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mMarkerViews.isEmpty()) {
                    Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_maps_indicator_current_position);
                    for (Marker m : mMarkers) {
                        Log.d(TAG, "Adding: " + m);
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        lp.leftMargin = m.getX() - (b.getWidth() / 2);
                        lp.topMargin = m.getY() - (b.getHeight() / 2);
                        lp.gravity = Gravity.TOP | Gravity.LEFT;

                        ImageView iv = new ImageView(getContext());
                        iv.setTag(m);
                        iv.setImageResource(R.drawable.ic_maps_indicator_current_position);
                        iv.setLayoutParams(lp);

                        if (mOnMarkerOnClickListener != null) {
                            iv.setOnClickListener(mOnMarkerOnClickListener);
                        }

                        mMarkerViews.add(iv);
                    }
                }
                for (ImageView iv : mMarkerViews) {
                    mContainer.addView(iv, iv.getLayoutParams());
                }
            }
        }.execute((Void[]) null);
    }

    private ImageView getNewTile(Tile tile) throws IOException {
        ImageView iv = new ImageView(getContext());

        ConfigurationSet set = getCurrentConfigurationSet();

        InputStream is;
        String path = set.getFilePattern().replace("%col%", new Integer(tile.y).toString())
                .replace("%row%", new Integer(tile.x).toString());
        try {
            is = getResources().getAssets().open(path);
            Bitmap bm = BitmapFactory.decodeStream(is);
            iv.setImageBitmap(bm);
            iv.setMinimumWidth(bm.getWidth());
            iv.setMinimumHeight(bm.getHeight());
            iv.setMaxWidth(bm.getWidth());
            iv.setMaxHeight(bm.getHeight());
            is.close();
        } catch (IOException e) {
            throw new IOException("Cannot open asset at:" + path);
        }

        iv.setTag(tile);

        return iv;
    }

    public void cleanupOldTiles() {
        Log.d(TAG, "Cleanup old tiles");

        Rect actualRect = new Rect(
                getScrollX(), getScrollY(),
                getWidth() + getScrollX(),
                getHeight() + getScrollY()
        );

        for (Tile tile : tiles.keySet()) {
            final ImageView v = tiles.get(tile).get();
            Rect r = new Rect();
            v.getHitRect(r);

            if (!Rect.intersects(actualRect, r)) {
                mContainer.removeView(v);
                tiles.remove(tile);
            }
        }
    }

    private boolean inZoomMode = false;
    private boolean ignoreLastFinger = false;
    private float mOrigSeparation;
    private static final float ZOOMJUMP = 75f;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction() & MotionEvent.ACTION_MASK;
        if (e.getPointerCount() == 2) {
            inZoomMode = true;
        } else {
            inZoomMode = false;
        }
        if (inZoomMode) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    // We may be starting a new pinch so get ready
                    mOrigSeparation = calculateSeparation(e);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    // We're ending a pinch so prepare to
                    // ignore the last finger while it's the
                    // only one still down.
                    ignoreLastFinger = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // We're in a pinch so decide if we need to change
                    // the zoom level.
                    float newSeparation = calculateSeparation(e);
                    TiledScrollView.ZoomLevel next = mCurrentZoomLevel;
                    if (newSeparation - mOrigSeparation > ZOOMJUMP) {
                        Log.d(TAG, "Zoom In!");

                        next = mCurrentZoomLevel.upLevel();
                        mOrigSeparation = newSeparation;
                    } else if (mOrigSeparation - newSeparation > ZOOMJUMP) {
                        Log.d(TAG, "Zoom Out!");

                        next = mCurrentZoomLevel.downLevel();
                        mOrigSeparation = newSeparation;
                    }

                    changeZoomLevel(next);

                    break;
            }
            // Don't pass these events to Android because we're
            // taking care of them.
            return true;
        } else {
            // cleanup if necessary from zooming logic
        }
        // Throw away events if we're on the last finger
        // until the last finger goes up.
        if (ignoreLastFinger) {
            if (action == MotionEvent.ACTION_UP)
                ignoreLastFinger = false;
            return true;
        }
        return super.onTouchEvent(e);
    }

    private void changeZoomLevel(TiledScrollView.ZoomLevel next) {
        if (next != mCurrentZoomLevel && mConfigurationSets.containsKey(next)) {
            mCurrentZoomLevel = next;
            Log.d(TAG, "new zoom level: " + mCurrentZoomLevel);

            tiles.clear();

            double x = getScrollX();
            double y = getScrollY();
            double w = mContainer.getWidth();
            double h = mContainer.getHeight();

            removeAllViews();

            init();

            double newW = getCurrentConfigurationSet().getImageWidth();
            double newH = getCurrentConfigurationSet().getImageHeight();

            Log.d(TAG, "1: " + x + ", " + y);
            Log.d(TAG, "2: " + w + ", " + h);
            Log.d(TAG, "3: " + newW + ", " + newH);

            Log.d(TAG, "new sX: " + (int) x / w * newW);
            Log.d(TAG, "new sY: " + (int) y / h * newH);

            smoothScrollTo((int) (x / w * newW), (int) (y / h * newH));

            if (onZoomLevelChangedListener != null) {
                onZoomLevelChangedListener.onZoomLevelChanged(mCurrentZoomLevel);
            }

            try {
                fillTiles();
            } catch (IOException e1) {
                Log.e(TAG, "Problem loading new tiles.", e1);
            }
        }
    }

    private float calculateSeparation(MotionEvent e) {
        float x = e.getX(0) - e.getX(1);
        float y = e.getY(0) - e.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    public boolean canZoomFurtherDown() {
        return mCurrentZoomLevel.downLevel() != mCurrentZoomLevel &&
                mConfigurationSets.containsKey(mCurrentZoomLevel.downLevel());
    }

    public void zoomDown() {
        changeZoomLevel(mCurrentZoomLevel.downLevel());
    }

    public boolean canZoomFurtherUp() {
        return mCurrentZoomLevel.upLevel() != mCurrentZoomLevel &&
                mConfigurationSets.containsKey(mCurrentZoomLevel.upLevel());
    }

    public void zoomUp() {
        changeZoomLevel(mCurrentZoomLevel.upLevel());
    }

    public class Marker {
        private int x;
        private int y;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public String getDescription() {
            return description;
        }

        private String description;

        public Marker(int x, int y, String description) {
            this.x = x;
            this.y = y;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Marker{" +
                    "x=" + x +
                    ", y=" + y +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
