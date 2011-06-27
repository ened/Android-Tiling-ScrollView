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
    static final int UPDATE_TILES = 123;
    static final int CLEANUP_OLD_TILES = 124;
    static final int FILL_TILES_DELAY = 200;

    private Animation fadeIn = AnimationUtils.loadAnimation(
            TiledScrollViewDemo.getRealApplicationContext(), R.anim.fadein);

    private FrameLayout mContainer;
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

    private Map<Tile, WeakReference<ImageView>> tiles = new ConcurrentHashMap<Tile, WeakReference<ImageView>>();

    private int mImageWidth;
    private int mImageHeight;
    private int mTileWidth;
    private int mTileHeight;

    public TiledScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        readAttributes(attrs);

        init();
    }

    public TiledScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        readAttributes(attrs);

        init();
    }

    private void readAttributes(AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView);

        mImageWidth = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_imageWidth, -1);
        mImageHeight = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_imageHeight, -1);
        mTileWidth = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_tileWidth, -1);
        mTileHeight = a.getInt(R.styleable.asia_ivity_android_tiledscrollview_TiledScrollView_tileHeight, -1);

        if (mImageWidth == -1 || mImageHeight == -1 || mTileWidth == -1 || mTileHeight == -1) {
            throw new IllegalArgumentException("Please set all attributes correctly!");
        }
    }

    private void init() {
        mContainer = new FrameLayout(getContext());

        final LayoutParams lp = new LayoutParams(mImageWidth, mImageHeight);

        // Required?
        mContainer.setMinimumWidth(mImageWidth);
        mContainer.setMinimumHeight(mImageHeight);
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

        mHandler.sendMessageDelayed(msg, FILL_TILES_DELAY);
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
            int tileY = new Double(Math.ceil(y / mTileHeight)).intValue();
            for (int x = left; x < width; ) {
                int tileX = new Double(Math.ceil(x / mTileWidth)).intValue();

                final Tile tile = new Tile(tileX, tileY);

                if (!tiles.containsKey(tile) || tiles.get(tile).get() == null) {
                    final ImageView iv = getNewTile(tileX, tileY);
                    iv.setId(new Random().nextInt());
                    FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(mTileWidth, mTileHeight);
                    lp2.leftMargin = tileX * mTileWidth;
                    lp2.topMargin = tileY * mTileHeight;
                    lp2.gravity = Gravity.TOP | Gravity.LEFT;
                    iv.setLayoutParams(lp2);

                    mContainer.addView(iv, lp2);
                    tiles.put(tile, new WeakReference<ImageView>(iv));

//                Message msg = Message.obtain();
//                msg.what = 111;
//                msg.arg1 = iv.getId();

//                mHandler.sendMessage(msg);
                }

                x = x + mTileWidth;
            }
            y = y + mTileHeight;
        }
    }

    private ImageView getNewTile(int x, int y) {
        ImageView iv = new ImageView(getContext());

        iv.setImageResource(getResources().getIdentifier("crop_" + y + "_" + x, "drawable",
                TiledScrollViewDemo.class.getPackage().getName()));

        // Required?
        iv.setMinimumHeight(mTileHeight);
        iv.setMinimumWidth(mTileWidth);

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

            if (!Rect.intersects(actualRect, r)) {
                mContainer.removeView(v);
            }
            tiles.remove(tile);
        }
    }

    /** Simple tile coordinates (X, Y). */
    private class Tile extends Pair<Integer, Integer> {
        /** {@inheritDoc} */
        public Tile(Integer first, Integer second) {
            super(first, second);
        }
    }
}
