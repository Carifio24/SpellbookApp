package dnd.jon.spellbook;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

class SortDirectionButton extends android.support.v7.widget.AppCompatImageButton {

    // Image for the up and down arrows
    static private int upArrow = R.drawable.up_arrow;
    static private int downArrow = R.drawable.down_arrow;

    // Enum indicating directions that the arrow can point
    enum Direction {
        Down, Up;

        // Returns the opposite direction
        Direction opposite() {
            return (this == Down) ? Up : Down;
        }
    }

    ///// Member values
    Direction direction;    // Direction that this button's arrow is pointing


    ///// Constructors

    public SortDirectionButton(Context context, Direction dir) {
        super(context);
        direction = dir;
    }

    public SortDirectionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        direction = Direction.Down;
    }

    // Pointing down is the default
    public SortDirectionButton(Context context) { this(context, Direction.Down); }


    ///// Methods

    // Update the image to reflect the current direction
    private void updateImage() {
        System.out.println("Update image");
        switch (direction) {
            case Up:
                setImageResource(upArrow);
                break;
            case Down:
                setImageResource(downArrow);
        }
    }

    // Toggle the button's direction
    // This involves both:
    // - Flipping the direction
    // - Updating the image accordingly
    private void toggle() {
        direction = direction.opposite();
        updateImage();
    }

    // When the button is pressed, we toggle its direction
    void onPress() { toggle(); }


    // Allow other objects to determine whether or not the button is pointing in each direction
    public boolean pointingUp() { return (direction == Direction.Up); }
    public boolean pointingDown() { return (direction == Direction.Down); }

    // Set the button's direction (used by MainActivity when loading settings)
    void setUp() {
        // Only need to do something if currently set to down
        if (direction == Direction.Down) { toggle(); }
    }

    void setDown() {
        // Only need to do something if currently set to up
        if (direction == Direction.Up) { toggle(); }
    }

}