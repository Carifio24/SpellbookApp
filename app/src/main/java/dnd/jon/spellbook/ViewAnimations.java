package dnd.jon.spellbook;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

class ViewAnimations {

    static void doAnimation(Context context, View view, int animationID) {
        Animation a = AnimationUtils.loadAnimation(context, animationID);
        if (a != null) {
            a.reset();
            if (view != null) {
                view.clearAnimation();
                view.startAnimation(a);
            }
        }
    }

    static void slideUp(Context context, View view) { doAnimation(context, view, R.anim.slide_up); }
    static void slideDown(Context context, View view) { doAnimation(context, view, R.anim.slide_down); }

    static void setExpandableHeader(Context context, View headerView, View expandableView, Runnable initialEffects, Runnable finalEffects) {
        headerView.setOnClickListener( (v) -> {
            initialEffects.run();
            if (expandableView.getVisibility() == View.VISIBLE) {
                slideUp(context, expandableView);
                final long toWait = (long) Math.ceil(0.75 * context.getResources().getInteger(R.integer.expand_contract_duration));
                v.postDelayed(() -> {
                    expandableView.setVisibility(View.GONE);
                    finalEffects.run();
                }, toWait);
            } else {
                expandableView.setVisibility(View.VISIBLE);
                slideDown(context, expandableView);
                finalEffects.run();
            }
        });
    }

    static void setExpandableHeader(Context context, View headerView, View expandableView) {
        Runnable runnable = () -> {};
        setExpandableHeader(context, headerView, expandableView, runnable, runnable);
    }

}
