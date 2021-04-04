//package dnd.jon.spellbook;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.CheckBox;
//import android.widget.GridLayout;
//import android.widget.LinearLayout;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.DialogFragment;
//
//public class SpellSlotManagerDialog extends DialogFragment {
//
//    private Activity activity;
//    private CharacterProfile cp;
//
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        super.onCreateDialog(savedInstanceState);
//
//        activity = requireActivity();
//        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View view = inflater.inflate(R.layout.spell_slot_manager, null);
//        builder.setView(view);
//
//        final AlertDialog d = builder.create();
//        final LinearLayout rowLayout = view.findViewById(R.id.spell_slots_rows);
//        for (int i = 0; i < Spellbook.MAX_SPELL_LEVEL; ++i) {
//            final int totalSlots = cp.getTotalSlots(i+1);
//            if (totalSlots > 0) {
//                rowLayout.addView(makeRow(i+1, totalSlots, cp.getAvailableSlots(i+1)));
//            }
//        }
//        return d;
//    }
//
//    LinearLayout makeRow(int level, int totalSlots, int availableSlots) {
//        final LinearLayout ll = new LinearLayout(activity);
//        final int usedSlots = totalSlots - availableSlots;
//        for (int i = 0; i < totalSlots; ++i) {
//            final CheckBox checkBox = new CheckBox(activity);
//            checkBox.setChecked(i < usedSlots);
//            checkBox.setTag(level);
//            ll.addView(checkBox);
//        }
//        return ll;
//    }
//
//}
