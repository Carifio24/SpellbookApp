package dnd.jon.spellbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class NavExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> groupNames;
    private final Map<String, List<String>> childData;
    private final Map<String, List<Integer>> childTextIDs;

    NavExpandableListAdapter(Context context, List<String> groupNames, Map<String, List<String>> childData, Map<String, List<Integer>> childTextIDs) {
        this.context = context;
        this.groupNames = groupNames;
        this.childData = childData;
        this.childTextIDs = childTextIDs;
    }

    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(groupNames.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(groupNames.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.right_nav_header, null);
        }
        final TextView header = convertView.findViewById(R.id.header);
        header.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.right_nav_submenu, null);
        }

        final TextView childTV = convertView.findViewById(R.id.submenu);
        childTV.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    int childTextID(int groupPosition, int childPosition) {
        return childTextIDs.get(groupNames.get(groupPosition)).get(childPosition);
    }

}
