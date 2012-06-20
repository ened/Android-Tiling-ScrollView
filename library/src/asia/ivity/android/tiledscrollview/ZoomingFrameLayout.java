package asia.ivity.android.tiledscrollview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * DESCRIPTION
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class ZoomingFrameLayout extends FrameLayout {
    public ZoomingFrameLayout(Context context) {
        super(context);
    }

    public ZoomingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomingFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(10, 10);
        canvas.rotate(5);

        super.onDraw(canvas);

        canvas.restore();
    }
}
