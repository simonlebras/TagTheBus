package fr.simonlebras.tagthebus.presentation.pictures;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint;
    private final int alpha;

    DividerItemDecoration(@ColorInt int color, float width) {
        paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(width);

        alpha = paint.getAlpha();
    }

    @Override
    public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
        final int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (position < state.getItemCount()) {
            outRect.set(0, 0, 0, (int) paint.getStrokeWidth());
        } else {
            outRect.setEmpty();
        }
    }

    @Override
    public void onDraw(final Canvas c, final RecyclerView parent, final RecyclerView.State state) {
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            final int position = params.getViewAdapterPosition();
            if (position < state.getItemCount()) {
                final int positionY = (int) (view.getBottom() + paint.getStrokeWidth() / 2 + view.getTranslationY());
                paint.setAlpha((int) (view.getAlpha() * alpha));

                c.drawLine(view.getLeft() + view.getTranslationX(), positionY, view.getRight() + view.getTranslationX(), positionY, paint);
            }
        }
    }
}
