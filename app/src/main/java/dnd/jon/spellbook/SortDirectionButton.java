package dnd.jon.spellbook;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;

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

    SortDirectionButton(Context context, Direction dir) {
        super(context);
        direction = dir;
    }

    // Pointing down is the default
    SortDirectionButton(Context context) { this(context, Direction.Down); }


    ///// Methods

    // Update the image to reflect the current direction
    void updateImage() {
        switch (direction) {
            case Up:
                setImageResource(upArrow);
            case Down:
                setImageResource(downArrow);
        }
    }

    // When the button is pressed, we both
    // - Flip the direction
    // - Update the image accordingly
    void onPress() {
        direction = direction.opposite();
        updateImage();
    }

    // Allow other objects to determine whether or not the button is pointing in each direction
    boolean pointingUp() { return (direction == Direction.Up); }
    boolean pointingDown() { return (direction == Direction.Down); }

}
