package dnd.jon.spellbook;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;

public class NonDraggableAppBarBehavior extends AppBarLayout.Behavior {
    public NonDraggableAppBarBehavior() {
        super();
        init();
    }

    public NonDraggableAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setDragCallback(new DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }
}