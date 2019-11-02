package music.symphony.com.materialmusicv2.utils.misc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class DragSortRecycler extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {

    private int itemDragBackgroundColor;

    private int selectedDragItemPos = -1;

    private int fingerAnchorY;

    private int fingerY;

    private int fingerOffsetInViewY;

    private BitmapDrawable floatingItem;
    private Rect floatingItemStatingBounds;
    private Rect floatingItemBounds;

    private int viewHandleId = -1;

    private OnItemMovedListener moveInterface;

    public DragSortRecycler(int itemDragBackgroundColor) {
        this.itemDragBackgroundColor = itemDragBackgroundColor;
    }

    private boolean isDragging;
    @Nullable
    private OnDragStateChangedListener dragStateChangedListener = new OnDragStateChangedListener() {
        @Override
        public void onDragStart() {
        }

        @Override
        public void onDragStop() {
        }
    };
    private Paint bgColor = new Paint();
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            fingerAnchorY -= dy;
        }
    };

    public RecyclerView.OnScrollListener getScrollListener() {
        return scrollListener;
    }

    public void setOnItemMovedListener(OnItemMovedListener swif) {
        moveInterface = swif;
    }

    public void setViewHandleId(int id) {
        viewHandleId = id;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView rv, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, rv, state);

        if (selectedDragItemPos != -1) {
            int itemPos = rv.getChildLayoutPosition(view);

            if (itemPos == selectedDragItemPos) {
                view.setVisibility(View.INVISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);

                float floatMiddleY = floatingItemBounds.top + (float) (floatingItemBounds.height() / 2.0);

                if ((itemPos > selectedDragItemPos) && (view.getTop() < floatMiddleY)) {
                    float amountUp = (floatMiddleY - view.getTop()) / (float) view.getHeight();
                    if (amountUp > 1)
                        amountUp = 1;

                    outRect.top = -(int) (floatingItemBounds.height() * amountUp);
                    outRect.bottom = (int) (floatingItemBounds.height() * amountUp);
                }

                if ((itemPos < selectedDragItemPos) && (view.getBottom() > floatMiddleY)) {
                    float amountDown = ((float) view.getBottom() - floatMiddleY) / (float) view.getHeight();
                    if (amountDown > 1)
                        amountDown = 1;

                    outRect.top = (int) (floatingItemBounds.height() * amountDown);
                    outRect.bottom = -(int) (floatingItemBounds.height() * amountDown);
                }
            }
        } else {
            outRect.top = 0;
            outRect.bottom = 0;
            view.setVisibility(View.VISIBLE);
        }
    }

    private int getNewPostion(RecyclerView rv) {
        int itemsOnScreen = Objects.requireNonNull(rv.getLayoutManager()).getChildCount();

        float floatMiddleY = floatingItemBounds.top + (float) (floatingItemBounds.height() / 2.0);

        int above = 0;
        int below = Integer.MAX_VALUE;
        for (int n = 0; n < itemsOnScreen; n++) {
            View view = rv.getLayoutManager().getChildAt(n);

            if (view != null && view.getVisibility() != View.VISIBLE) continue;

            int itemPos = 0;
            if (view != null) {
                itemPos = rv.getChildLayoutPosition(view);
            }

            if (itemPos == selectedDragItemPos)
                continue;

            float viewMiddleY = 0;
            if (view != null) {
                viewMiddleY = view.getTop() + (float) (view.getHeight() / 2.0);
            }
            if (floatMiddleY > viewMiddleY) {
                if (itemPos > above)
                    above = itemPos;
            } else if (floatMiddleY <= viewMiddleY) {
                if (itemPos < below)
                    below = itemPos;
            }
        }

        if (below != Integer.MAX_VALUE) {
            if (below < selectedDragItemPos)
                below++;
            return below - 1;
        } else {
            if (above < selectedDragItemPos)
                above++;

            return above;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            View itemView = rv.findChildViewUnder(e.getX(), e.getY());

            if (itemView == null)
                return false;

            boolean dragging = false;

            if (viewHandleId != -1) {
                View handleView = itemView.findViewById(viewHandleId);

                if (handleView == null) {
                    Log.e("Recyclerview reorder", "The view id " + viewHandleId + " was not found in the RecyclerView item");
                    return false;
                }

                if (handleView.getVisibility() != View.VISIBLE) {
                    return false;
                }

                int[] parentItemPos = new int[2];
                itemView.getLocationInWindow(parentItemPos);

                int[] handlePos = new int[2];
                handleView.getLocationInWindow(handlePos);

                int xRel = handlePos[0] - parentItemPos[0];
                int yRel = handlePos[1] - parentItemPos[1];

                Rect touchBounds = new Rect(itemView.getLeft() + xRel, itemView.getTop() + yRel,
                        itemView.getLeft() + xRel + handleView.getWidth(),
                        itemView.getTop() + yRel + handleView.getHeight()
                );

                if (touchBounds.contains((int) e.getX(), (int) e.getY()))
                    dragging = true;
            }


            if (dragging) {
                setIsDragging(true);

                floatingItem = createFloatingBitmap(itemView);

                fingerAnchorY = (int) e.getY();
                fingerOffsetInViewY = fingerAnchorY - itemView.getTop();
                fingerY = fingerAnchorY;

                selectedDragItemPos = rv.getChildLayoutPosition(itemView);

                return true;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if ((e.getAction() == MotionEvent.ACTION_UP) ||
                (e.getAction() == MotionEvent.ACTION_CANCEL)) {
            if ((e.getAction() == MotionEvent.ACTION_UP) && selectedDragItemPos != -1) {
                int newPos = getNewPostion(rv);
                if (moveInterface != null)
                    moveInterface.onItemMoved(selectedDragItemPos, newPos);
            }

            setIsDragging(false);
            selectedDragItemPos = -1;
            floatingItem = null;
            rv.invalidateItemDecorations();
            return;
        }


        fingerY = (int) e.getY();

        if (floatingItem != null) {
            floatingItemBounds.top = fingerY - fingerOffsetInViewY;

            if (floatingItemBounds.top < -floatingItemStatingBounds.height() / 2)
                floatingItemBounds.top = -floatingItemStatingBounds.height() / 2;

            floatingItemBounds.bottom = floatingItemBounds.top + floatingItemStatingBounds.height();

            floatingItem.setBounds(floatingItemBounds);
        }

        float scrollAmount = 0;
        float autoScrollWindow = 0.1f;
        if (fingerY > (rv.getHeight() * (1 - autoScrollWindow))) {
            scrollAmount = (fingerY - (rv.getHeight() * (1 - autoScrollWindow)));
        } else if (fingerY < (rv.getHeight() * autoScrollWindow)) {
            scrollAmount = (fingerY - (rv.getHeight() * autoScrollWindow));
        }

        float autoScrollSpeed = 0.5f;
        scrollAmount *= autoScrollSpeed;
        rv.scrollBy(0, (int) scrollAmount);

        rv.invalidateItemDecorations();
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void setIsDragging(final boolean dragging) {
        if (dragging != isDragging) {
            isDragging = dragging;
            if (dragStateChangedListener != null) {
                if (isDragging) {
                    dragStateChangedListener.onDragStart();
                } else {
                    dragStateChangedListener.onDragStop();
                }
            }
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (floatingItem != null) {
            int floatingItemBgColor = itemDragBackgroundColor;
            bgColor.setColor(floatingItemBgColor);
            c.drawRect(floatingItemBounds, bgColor);
            floatingItem.draw(c);
        }
    }

    /**
     * @return True if we can drag the item over this position, False if not.
     */

    private BitmapDrawable createFloatingBitmap(View v) {
        floatingItemStatingBounds = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        floatingItemBounds = new Rect(floatingItemStatingBounds);

        Bitmap bitmap = Bitmap.createBitmap(floatingItemStatingBounds.width(),
                floatingItemStatingBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        BitmapDrawable retDrawable = new BitmapDrawable(v.getResources(), bitmap);
        retDrawable.setBounds(floatingItemBounds);

        return retDrawable;
    }

    public interface OnItemMovedListener {
        void onItemMoved(int from, int to);
    }

    public interface OnDragStateChangedListener {
        void onDragStart();

        void onDragStop();
    }
}
