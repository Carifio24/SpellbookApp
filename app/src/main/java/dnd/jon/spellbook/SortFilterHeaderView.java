package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import java.util.function.BiConsumer;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.SortFilterHeaderBinding;

public class SortFilterHeaderView extends ConstraintLayout {

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

    private final SortFilterHeaderBinding binding;

    // Constructors
    // First constructor is public so that it can be used via XML
    public SortFilterHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = SortFilterHeaderBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        addView(binding.getRoot());
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SortFilterHeaderView, 0, 0);
        final String title = SpellbookUtils.coalesce(a.getString(R.styleable.SortFilterHeaderView_title), "");
        final String infoTitle = SpellbookUtils.coalesce(a.getString(R.styleable.SortFilterHeaderView_info_title), "");
        final String infoDescription = SpellbookUtils.coalesce(a.getString(R.styleable.SortFilterHeaderView_info_description), "");
        a.recycle();

        binding.setTitle(title);
        binding.setInfoTitle(infoTitle);
        binding.setInfoDescription(infoDescription);
        binding.executePendingBindings();

        //final AppCompatTextView titleView = binding.headerTitle;
        //final ImageView imageView = binding.headerExpansionButton;

        // Create the drawables
        minusDrawable = ResourcesCompat.getDrawable(getResources(), minusID, null);
        plusDrawable = ResourcesCompat.getDrawable(getResources(), plusID, null);

        // Image setup
        binding.headerExpansionButton.setImageDrawable(minusDrawable);

        // On click action
        doOnClick  = () -> {
            expanded = !expanded;
            final Drawable image = expanded ? minusDrawable : plusDrawable;
            binding.headerExpansionButton.setImageDrawable(image);
        };

        // Setting up the constraints
//        final ConstraintSet constraintSet = new ConstraintSet();
//        final int imageID = imageView.getId();
//        final int titleID = titleView.getId();
//        constraintSet.clone(this);
//        constraintSet.connect(imageView.getId(), ConstraintSet.END, this.getId(), ConstraintSet.END, endMargin);
//        constraintSet.connect(imageView.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, topMargin);
//        constraintSet.connect(imageView.getId(), ConstraintSet.BOTTOM, this.getId(), ConstraintSet.BOTTOM, bottomMargin);
//        constraintSet.connect(titleView.getId(), ConstraintSet.START, this.getId(), ConstraintSet.START, startMargin);
//        constraintSet.connect(titleView.getId(), ConstraintSet.END, imageView.getId(), ConstraintSet.START, betweenMargin);
//        constraintSet.connect(titleView.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP, topMargin);
//        constraintSet.applyTo(this);

        // Apply the constraints
        //constraintSet.applyTo(this);

    }

    void setTitleSize(int size) { binding.headerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size); }

    private <T> void setBindingItem(BiConsumer<SortFilterHeaderBinding,T> binder, T item) {
        binder.accept(binding, item);
        binding.executePendingBindings();
    }
    void setTitle(String title) { setBindingItem(SortFilterHeaderBinding::setTitle, title); }
    void setInfoTitle(String infoTitle) { setBindingItem(SortFilterHeaderBinding::setInfoTitle, infoTitle); }
    void setInfoDescription(String infoDescription) { setBindingItem(SortFilterHeaderBinding::setInfoDescription, infoDescription); }

    @Override
    public void setOnClickListener(ConstraintLayout.OnClickListener listener) {
        super.setOnClickListener((v) -> {
            doOnClick.run();
            listener.onClick(v);
        });
    }

}
