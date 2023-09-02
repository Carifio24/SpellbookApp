package dnd.jon.spellbook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.javatuples.Pair;


public class ShortScrollbarRVScrollView extends ScrollView {
    public ShortScrollbarRVScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShortScrollbarRVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShortScrollbarRVScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShortScrollbarRVScrollView(Context context) {
        super(context);
    }

    // This is the value of scrollY when the RV is fully scrolled
    private int yScrollRange() {
        return super.computeVerticalScrollRange() - getHeight();
    }

    private Pair<Integer,Integer> findItemPositions() {
        final RecyclerView recyclerView = (RecyclerView) getChildAt(0);
        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int topPos = layoutManager.findFirstCompletelyVisibleItemPosition();
        int bottomPos = layoutManager.findLastCompletelyVisibleItemPosition();

        View view = layoutManager.findViewByPosition(topPos);
        while (view == null) {
            topPos += 1;
            view = layoutManager.findViewByPosition(topPos);
        }

        view = layoutManager.findViewByPosition(bottomPos);
        while (view == null) {
            bottomPos -= 1;
            view = layoutManager.findViewByPosition(bottomPos);
        }
        return new Pair<>(topPos, bottomPos);
    }

    final

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent() / 5;
    }

    @Override
    public int computeVerticalScrollOffset() {
        final Pair<Integer,Integer> positions = findItemPositions();
        final RecyclerView recyclerView = (RecyclerView) getChildAt(0);
        final int count = recyclerView.getAdapter().getItemCount();
        final int topPosition = positions.getValue0();
        final int bottomPosition = positions.getValue1();
        final int res = topPosition * (getHeight() - computeVerticalScrollExtent()) / (count - bottomPosition + topPosition - 1);
        return res;
    }

}
