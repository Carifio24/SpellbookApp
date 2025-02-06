package dnd.jon.spellbook;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import java.util.Arrays;

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
        view.getLocationInWindow(location);
        System.out.println(Arrays.toString(location));

        int[] screen = new int[2];
        view.getLocationOnScreen(screen);
        System.out.println(Arrays.toString(screen));

        System.out.printf("%d, %d\n", view.getLeft(), view.getTop());
        final Rect rect = new Rect();
        view.getFocusedRect(rect);
        System.out.println(rect);
        return location;
    }

    @Override
    public Point getPoint() {
        int[] topLeftLocation = locationForView(topLeftView);
        int[] bottomRightLocation = locationForView(bottomRightView);
        final int pointX = (topLeftLocation[0] + bottomRightLocation[0] + bottomRightView.getWidth()) / 2;
        final int pointY = (topLeftLocation[1] + bottomRightLocation[1] + bottomRightView.getHeight()) / 2;
        return new Point(pointX, pointY);
    }

    @Override
    public Rect getBounds() {
        int[] topLeftLocation = locationForView(topLeftView);
        int[] bottomRightLocation = locationForView(bottomRightView);
        final int rightValue = topLeftLocation[0] + bottomRightLocation[0] + bottomRightView.getMeasuredWidth();
        final int bottomValue = topLeftLocation[1] + bottomRightLocation[1] + bottomRightView.getMeasuredHeight();
        return new Rect(
            topLeftLocation[0],
            topLeftLocation[1],
            rightValue,
            bottomValue
        );
    }
}
