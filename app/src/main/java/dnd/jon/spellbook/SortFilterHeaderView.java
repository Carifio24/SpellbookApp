package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class SortFilterHeaderView extends ConstraintLayout {

    private final AppCompatTextView titleView;
    private final ImageView imageView;

    private static final int topMargin = 0;
    private static final int bottomMargin = topMargin;
    private static final int startMargin = 10;
    private static final int endMargin = startMargin;
    private static final int betweenMargin = 2;


    private static final int minusID = R.drawable.ic_remove;
    private static final int plusID = R.drawable.ic_add;
    private final Drawable minusDrawable;
    private final Drawable plusDrawable;
    private boolean expanded = true;
    private final Runnable doOnClick;

    // Constructors
    // First constructor is public so that it can be used via XML
    public SortFilterHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SortFilterHeaderView, 0, 0);
        final String title = SpellbookUtils.coalesce(a.getString(R.styleable.SortFilterHeaderView_title), "");
        a.recycle();

        // Create the title view and the button, and add them as subviews
        titleView = new AppCompatTextView(context);
        imageView = new ImageView(context);
        titleView.setId(View.generateViewId());
        imageView.setId(View.generateViewId());
        addView(titleView);
        addView(imageView);

        // Create the drawables
        minusDrawable = context.getResources().getDrawable(minusID, null);
        plusDrawable = context.getResources().getDrawable(plusID, null);

        // Title view setup
        titleView.setTextAppearance(context, R.style.SortFilterTitleStyle);
        titleView.setAlpha(0.54f);
        titleView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        titleView.setText(title);

        // Image setup
        imageView.setImageDrawable(minusDrawable);

        // On click action
        doOnClick  = () -> {
            expanded = !expanded;
            final Drawable image = expanded ? minusDrawable : plusDrawable;
            imageView.setImageDrawable(image);
        };

        // Setting up the constraints
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(imageView.getId(), ConstraintSet.END, this.getId(), ConstraintSet.END, endMargin);
        constraintSet.connect(imageView.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, topMargin);
        constraintSet.connect(imageView.getId(), ConstraintSet.BOTTOM, this.getId(), ConstraintSet.BOTTOM, bottomMargin);
        constraintSet.connect(titleView.getId(), ConstraintSet.START, this.getId(), ConstraintSet.START, startMargin);
        constraintSet.connect(titleView.getId(), ConstraintSet.END, imageView.getId(), ConstraintSet.START, betweenMargin);
        constraintSet.connect(titleView.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, topMargin);
        constraintSet.applyTo(this);

        // Apply the constraints
        constraintSet.applyTo(this);

    }

    void setTitleSize(int size) { titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size); }

    void setTitle(String title) {
        titleView.setText(title);
    }

    @Override
    public void setOnClickListener(ConstraintLayout.OnClickListener listener) {
        super.setOnClickListener((v) -> {
            doOnClick.run();
            listener.onClick(v);
        });
    }

}
