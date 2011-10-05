package asia.ivity.android.tiledscrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * DESCRIPTION
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class TiledScrollView extends FrameLayout {

    private TiledScrollViewWorker mScrollView;
    private ImageButton mBtnZoomDown;
    private ImageButton mBtnZoomUp;

    public TiledScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public TiledScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater lf = LayoutInflater.from(getContext());

        mScrollView = new TiledScrollViewWorker(getContext(), attrs);
        mScrollView.setOnZoomLevelChangedListener(new OnZoomLevelChangedListener() {
            public void onZoomLevelChanged(TiledScrollViewWorker.ZoomLevel newLevel) {
                updateZoomButtons();
            }
        });

        addView(mScrollView);

        View btns = lf.inflate(R.layout.ll_tiledscroll_view_zoom_buttons, this, false);

        mBtnZoomDown = (ImageButton) btns.findViewById(R.id.btn_zoom_down);
        mBtnZoomUp = (ImageButton) btns.findViewById(R.id.btn_zoom_up);

        mBtnZoomDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mScrollView.zoomDown();
            }
        });
        mBtnZoomUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mScrollView.zoomUp();
            }
        });

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(btns, params);

        updateZoomButtons();
    }

    public void addConfigurationSet(TiledScrollViewWorker.ZoomLevel level, ConfigurationSet set) {
        mScrollView.addConfigurationSet(level, set);

        updateZoomButtons();
    }

    private void updateZoomButtons() {
        if (!mScrollView.canZoomFurtherDown() && !mScrollView.canZoomFurtherUp()) {
            mBtnZoomDown.setVisibility(GONE);
            mBtnZoomUp.setVisibility(GONE);
        } else {
            mBtnZoomDown.setVisibility(VISIBLE);
            mBtnZoomUp.setVisibility(VISIBLE);
            mBtnZoomDown.setEnabled(mScrollView.canZoomFurtherDown());
            mBtnZoomUp.setEnabled(mScrollView.canZoomFurtherUp());
        }
    }

    public void cleanupOldTiles() {
        mScrollView.cleanupOldTiles();
    }
}
