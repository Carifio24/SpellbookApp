package dnd.jon.spellbook;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.ViewGroup;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class CenterReveal {

    private final View view;
    private final View container;
    private ObjectAnimator viewTranslation;
    private ObjectAnimator viewAlpha;
    private ObjectAnimator viewScale;
    private ObjectAnimator containerAlpha;

    private static final long duration = 150L;

    CenterReveal(View view, View container) {
        this.view = view;
        this.container = container;
        final ViewGroup parent = (ViewGroup) view.getParent();
        final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final float cX = marginLayoutParams.getMarginEnd() + view.getWidth() / 2f - parent.getWidth() / 2f;
        final float cY = marginLayoutParams.bottomMargin + view.getHeight() / 2f - parent.getHeight() / 2f;
        viewTranslation = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, cX),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, cY)
        );
        viewAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f);
        containerAlpha = ObjectAnimator.ofFloat(container, View.ALPHA, 0f, 1f);
        viewScale = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 10f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 10f)
        );
    }

    void start(Runnable transaction, Runnable onEnd) {
        //final ViewGroup parent = (ViewGroup) view.getParent();
        //final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        // final float cX = marginLayoutParams.getMarginEnd() + view.getWidth() / 2f - parent.getWidth() / 2f;
        // final float cY = marginLayoutParams.bottomMargin + view.getHeight() / 2f - parent.getHeight() / 2f;

//        viewTranslation = ObjectAnimator.ofPropertyValuesHolder(view,
//                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, cX),
//                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, cY)
//        );
//        viewAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f);
//        containerAlpha = ObjectAnimator.ofFloat(container, View.ALPHA, 0f, 1f);
//        viewScale = ObjectAnimator.ofPropertyValuesHolder(view,
//                PropertyValuesHolder.ofFloat(View.SCALE_X, 10f),
//                PropertyValuesHolder.ofFloat(View.SCALE_Y, 10f)
//        );

        final AnimatorSet firstAnimatorSet = new AnimatorSet();
        firstAnimatorSet.setDuration(duration);
        firstAnimatorSet.setInterpolator(new FastOutSlowInInterpolator());
        firstAnimatorSet.play(viewTranslation);
        firstAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (transaction != null) { transaction.run(); }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        final AnimatorSet secondAnimatorSet = new AnimatorSet();
        secondAnimatorSet.setDuration(duration);
        secondAnimatorSet.playTogether(viewAlpha, viewScale, containerAlpha);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(firstAnimatorSet, secondAnimatorSet);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
                if (onEnd != null) { onEnd.run(); }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    void start(Runnable transaction) { start(transaction, null); }

    void reverse(Runnable onEnd) {
        viewTranslation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (onEnd != null) { onEnd.run(); }
                viewTranslation.removeAllListeners();
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        viewScale.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                viewScale.removeAllListeners();
                viewTranslation.reverse();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        viewScale.setDuration(duration).reverse();
        viewAlpha.setDuration(duration).reverse();
        containerAlpha.setDuration(duration).reverse();
    }

}
