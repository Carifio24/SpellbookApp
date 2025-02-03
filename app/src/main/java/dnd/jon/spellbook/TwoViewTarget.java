package dnd.jon.spellbook;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import uk.co.deanwild.materialshowcaseview.target.Target;

public class TwoViewTarget implements Target {
    private final View topLeftView;
    private final View bottomRightView;

    public TwoViewTarget(View topLeftView, View bottomRightView) {
        this.topLeftView = topLeftView;
        this.bottomRightView = bottomRightView;
    }

    private int[] locationForView(View view) {
        int[] location = new int[2];
        topLeftView.getLocationInWindow(location);
        return location;
    }

    @Override
    public Point getPoint() {
        int[] topLeftLocation = locationForView(topLeftView);
        int[] bottomRightLocation = locationForView(bottomRightView);
        return new Point(
            (topLeftLocation[0] + bottomRightLocation[0] + bottomRightView.getWidth()) / 2,
            (topLeftLocation[1] + bottomRightLocation[1] + bottomRightView.getHeight()) / 2
        );
    }

    @Override
    public Rect getBounds() {
        int[] topLeftLocation = locationForView(topLeftView);
        int[] bottomRightLocation = locationForView(bottomRightView);
        return new Rect(
            topLeftLocation[0],
            topLeftLocation[1],
            topLeftLocation[0] + bottomRightLocation[0] + bottomRightView.getMeasuredWidth(),
            topLeftLocation[1] + bottomRightLocation[1] + bottomRightView.getMeasuredHeight()
        );
    }
}
