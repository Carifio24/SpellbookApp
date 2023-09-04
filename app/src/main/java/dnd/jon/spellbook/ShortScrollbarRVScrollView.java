package dnd.jon.spellbook;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

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

    @Override
    public int computeVerticalScrollExtent() {
        final int yRange = yScrollRange();
        if (yRange <= 0) {
            return 0;
        }
        return super.computeVerticalScrollExtent() / 5;
    }

    @Override
    public int computeVerticalScrollOffset() {
        final int yRange = yScrollRange();
        if (yRange <= 0) {
            return 0;
        }
        return getScrollY() * (getHeight() - computeVerticalScrollExtent()) / yRange;
    }

}
