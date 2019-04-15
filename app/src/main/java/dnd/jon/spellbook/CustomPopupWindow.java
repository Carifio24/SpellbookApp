package dnd.jon.spellbook;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

abstract class CustomPopupWindow {

    MainActivity main;
    View popupView;
    PopupWindow popup;
    Context popupContext;

    private static int defaultWidth = 1000;
    private static int defaultHeight = 1000;
    private static boolean defaultFocusable = true;

    CustomPopupWindow(MainActivity m, int layoutID, int width, int height, boolean focusable) {
        main = m;
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(layoutID, null);
        popup = new PopupWindow(popupView, width, height, focusable);
        popupContext = popupView.getContext();
        popup.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    CustomPopupWindow(MainActivity m, int layoutID) {
        this(m, layoutID, defaultWidth, defaultHeight, defaultFocusable);
    }

    void show() {
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    void dismiss() {
        popup.dismiss();
    }


}
