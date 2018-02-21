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

/**
 * Dient als verbindung zwichen den Lesson.LESSONS Array und der ExpandableListView
 * Quelle: https://www.youtube.com/watch?v=0FJUwpnjScQ && https://www.youtube.com/watch?v=upYp631sffc
 */
public class ExpandableListViewAdapter  extends BaseExpandableListAdapter {

    //Dient zum speichern der Überschriften
    private List<String> groupNames = new ArrayList<>();
    //Sortiertes speichern der Schulstunden im Bezug auf die ExpandableListView
    private List<List<Lesson>> childNames = new ArrayList<>();
    //Erforderlich, zum erstellen neuer Views, wie z.B. TextViews
    private Context context;

    /**
     * Konstruktor, zum füllen der internen ArrayLists und dem Deklarieren des Contexts
     * @param context Application Context, der zum erstellen neuer View benötigt wird
     */
    ExpandableListViewAdapter(Context context) {
        this.context = context;
        //Füllen der ArrayListts
        for (Lesson lesson: Lesson.LESSONS ) {
            //Titel der Gruppen generieren
            String groupTitle = lesson.getGroupText();
            //Prüfen, ob der Titel schon exsistiert
            if (!groupNames.contains(groupTitle)) {
                //Titel in ArrayList eintragen, wenn er nicht exsistiert
                groupNames.add(groupTitle);
                //Neue List<Lesson> erstellen (quasi als Inhalt der Gruppe zu betrachten)
                childNames.add(new ArrayList<Lesson>());
            }

            //abrufen der Unterliste an den Koordinaten der Überschrift
            List<Lesson> childs = childNames.get(groupNames.indexOf(groupTitle));
            //Prüfen, ob dies erfolgreich war
            if (childs != null)
                //Hinzufügen der aktuell eingelesenen Schulstunde in Überkategorie
                childs.add(lesson);
        }
        //Log-Ausgaben zu Debugzwecken
        Log.i("Gruppenliste" , groupNames.size() + "");
        Log.i("Childliste" , childNames.size() + "");
        for (List<Lesson> childs: childNames) {
            Log.i("childs" , childs.size() + "");
        }

    }

    /**
     * Wird in der BaseExpandableListAdapter abstract deklariert und muss daher implementiert werden
     * @return gibt die Anzahl der Gruppen wieder
     */
    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @param groupPosition die Gruppe, aus der die childs genommen werden
     * @return gibt die Anzahl der Childs in der angegebenen Gruppe
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return childNames.get(groupPosition).size();
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @param groupPosition die position, der Gruppe, die ausgegeben werden soll
     * @return die geforderte Gruppe
     */
    @Override
    public Object getGroup(int groupPosition) {
        return groupNames.get(groupPosition);
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @param groupPosition gruppe, in der das child ist
     * @param childPosition der index des childs das geforddert wird
     * @return
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childNames.get(groupPosition).get(childPosition);
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @param groupPosition gruppe, dessen ID gefordert ist
     * @return index wird als ID zurückgegeben
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @param groupPosition Gruppe, in der das child ist
     * @param childPosition childindex , dessen ID gefordert wird
     * @return index wird als id zurückgegeben
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * @return gibt an, ob sich die IDs ändern können oder ob sie statisch sind
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * Generiert ein View, welches als Gruppe ausgegeben wird
     * @param groupPosition Position (Index) der geforderten Gruppe
     * @param isExpanded Ist die Gruppe geöffnet?
     * @param view
     * @param parent
     * @return View, welches ausgegeben wird
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        //Neues View initialisieren
        TextView txtView = new TextView(context);
        //Text des View setzen
        txtView.setText(groupNames.get(groupPosition));
        //Paddings zum Rand setzen
        txtView.setPadding(100,0,0,0);
        //Schriftgröße setzen
        txtView.setTextSize(30);
        //View zurückggeben
        return txtView;
    }

    /**
     * Wird in der BaseExpandableListViewAdapter abstract deklariert und muss daher implementiert werden
     * Generiert ein View, welches genutzt wird um childs dar zu stellen
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param view
     * @param parent
     * @return
     */
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
