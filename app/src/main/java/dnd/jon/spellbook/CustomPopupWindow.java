package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

abstract class CustomPopupWindow {

    final Context activity;
    final View popupView;
    final PopupWindow popup;

    private static final boolean defaultFocusable = true;

    CustomPopupWindow(Context activity, int layoutID, boolean focusable) {
        this.activity = activity;
        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(layoutID, null);
        popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, focusable);
        popup.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    CustomPopupWindow(Context activity, int layoutID) {
        this(activity, layoutID, defaultFocusable);
    }

    void showUnderView(View view) {
        final int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        final int height = view.getHeight();
        final int x = viewLocation[0];
        final int y = viewLocation[1] + height;
        popup.showAtLocation(popupView, Gravity.TOP | Gravity.START, x, y);
    }

    void dismiss() {
        popup.dismiss();
    }

}
