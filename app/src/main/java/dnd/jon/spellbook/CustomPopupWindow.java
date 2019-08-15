package dnd.jon.spellbook;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

abstract class CustomPopupWindow {

    MainActivity main;
    View popupView;
    PopupWindow popup;
    Context popupContext;

    private static boolean defaultFocusable = true;

    CustomPopupWindow(MainActivity m, int layoutID, boolean focusable) {
        main = m;
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(layoutID, null);
        //popup = new PopupWindow(popupView, width, height, focusable);
        popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, focusable);
        popupContext = popupView.getContext();
        popup.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    CustomPopupWindow(MainActivity m, int layoutID) {
        this(m, layoutID, defaultFocusable);
    }

    void show() {
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    void showUnderView(View view) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int height = view.getHeight();
        int x = viewLocation[0];
        //int y = viewLocation[1] + (int) Math.round(height * 0.8);
        int y = viewLocation[1] + height;
        popup.showAtLocation(popupView, Gravity.TOP | Gravity.START, x, y);
    }

    void dismiss() {
        popup.dismiss();
    }


}
