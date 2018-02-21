package de.eskes.bbs_quicksearch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListViewAdapter  extends BaseExpandableListAdapter {

    private List<String> groupNames = new ArrayList<>();
    private List<List<Lesson>> childNames = new ArrayList<>();

    private Context context;

    ExpandableListViewAdapter(Context context) {
        this.context = context;

        for (Lesson lesson: Lesson.LESSONS ) {
            String groupTitle = lesson.getGroupText();
            if (!groupNames.contains(groupTitle)) {
                groupNames.add(groupTitle);
                childNames.add(new ArrayList<Lesson>());
            }

            List<Lesson> childs = childNames.get(groupNames.indexOf(groupTitle));
            if (childs != null)
                childs.add(lesson);
        }
        Log.i("Gruppen" , groupNames.size() + "");
        Log.i("childs" , childNames.size() + "");
        for (List<Lesson> childs: childNames) {
            Log.i("child " , childs.size() + "");
        }

    }

    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childNames.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childNames.get(groupPosition).get(childPosition);
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
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        TextView txtView = new TextView(context);
        txtView.setText(groupNames.get(groupPosition));
        txtView.setPadding(100,0,0,0);
        txtView.setTextSize(30);
        return txtView;
    }

    @Override
    public View getChildView(final int groupPosition,final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        final Lesson lesson = childNames.get(groupPosition).get(childPosition);
        final TextView txtView = new TextView(context);
        txtView.setText(lesson.getChildText());
        txtView.setPadding(100,0,0,0);
        txtView.setTextSize(22);

        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setMessage(lesson.toString());
                alertDialog.setTitle(txtView.getText());
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        });

        return txtView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
