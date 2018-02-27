package de.eskes.bbs_quicksearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    ProgressDialog DIALOG = null;
    int lastExpanded = -1;

    /**
     * Löst aus, wenn die Activity wieder den Focus hat
     * Quelle: https://developer.android.com/guide/components/activities/activity-lifecycle.html (27.02.2018)
     */

    @Override
    protected void onResume(){
        super.onResume();

        //Initialisieren der Einstellungen
        //Quelle: https://developer.android.com/reference/android/content/SharedPreferences.html
        SharedPreferences settings = getSharedPreferences("Settings", 0);
        //Initialisieren der Volley queue
        //Quelle: https://developer.android.com/training/volley/index.html
        final RequestQueue queue = Volley.newRequestQueue(this);

        //Testen, ob die gespeicherten Anmeldedaten korrekt sind
        testCredentials(settings.getString("Pass", ""), queue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Laden der "Toolbar"
        //Quelle: https://developer.android.com/training/appbar/setting-up.html
        Toolbar toolbar = findViewById(R.id.toolbar);
        //"Toolbar" als ActionBar (Die obere blaue Leiste)
        setSupportActionBar(toolbar);

        //Initialisieren der Volley queue
        final RequestQueue queue = Volley.newRequestQueue(this);
        //Initialisieren der Einstellungen
        final SharedPreferences settings = getSharedPreferences("Settings", 0);

        //Initialisieren der Steuerelemente
        final EditText txtSearchterm =  findViewById(R.id.txtSearchterm);
        final Button btnSearch =        findViewById(R.id.btnSearch);

        //Eintragen des Letzten Suchbegriffs
        txtSearchterm.setText(settings.getString("Term", ""));

        //Verlegt in die onResume() Methode
        //testCredentials(settings.getString("Pass", ""), queue);

        //onClick Event abfangen
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //Dient zum ausblenden der Tastatur beim drücken des Suchen buttons
                    //Quelle: https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard (27.02.2018)
                    InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                }
                catch (Exception x){
                    //Uninteressante Logmeldung
                    Log.e("MainActivity", "No focused View");
                }

                //Initialisieren des Editor zum schreiben der Einstellungen
                //Quelle: https://developer.android.com/reference/android/content/SharedPreferences.Editor.html (27.02.2018)
                SharedPreferences.Editor editor = settings.edit();
                //Speicher des zuletzt eingegebenen Suchbegriffes
                editor.putString("Term", txtSearchterm.getText().toString());
                //Anwenden der Änderungen
                editor.apply();

                //Zurücksetzen der Lessons Liste
                Lesson.LESSONS.clear();

                //URL an die die Anfrage geschickt wird generieren
                String url = "https://eskes.de/janmarcus/Server/index.php?output=XML2&password=" + settings.getString("Pass","") + "&search=" + txtSearchterm.getText().toString() + "&sites=" + settings.getInt("Days", 5);

                //Zu Debugzwecken die URL ausgeben
                //Toast toast = Toast.makeText(getApplicationContext(),url, Toast.LENGTH_SHORT);
                //toast.show();

                //Erstellen der Volley Anfrage
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //speichern der Antwort des Servers
                        analyseXmlString(response);
                        Log.i("Response" ,response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Fehlermeldung, da der Server nicht antwortet
                        Toast toast = Toast.makeText(getApplicationContext(),R.string.downloadFail, Toast.LENGTH_LONG);
                        toast.show();
                        //Ausgeben der Fehlermeldung im Log
                        //Log.e("Volley", error.getLocalizedMessage());
                        //Download-Dialog schließen
                        DIALOG.cancel();
                    }
                });
                //Ausführen der Volley Anfrage
                queue.add(stringRequest);

                //Ladebildschirm
                //Quelle: https://stackoverflow.com/questions/2525683/how-to-create-loading-dialogs-in-android (27.02.2018)
                DIALOG = ProgressDialog.show(MainActivity.this, "Download", getString(R.string.loading), true);

            }
        });
    }

    /**
     * Wird genutzt, um den XML-Code, den der Server zurück gibt und der als Parameter übergeben wird in die Schulstunden zu Parsen
     * @param response Ist der XML-String, der geparst werden soll
     */
    public void analyseXmlString(String response){
        //Schließen des Ladedialoges
        DIALOG.cancel();
        //Parsen des XML in "Lessons"
        //Quelle: http://www.vogella.com/tutorials/AndroidXML/article.html (27.02.2018)

        //Festlegen der XML-Tags
        String tag = "Tag";
        String pos = "Pos";
        String lehrer = "Lehrer";
        String fach = "Fach";
        String raum = "Raum";
        String klasse = "Klasse";
        String vertreter = "Vertreter";
        String art = "Art";
        String info = "Info";
        String lesson = "Lesson";
        String refreshed = "Refreshed";
        String document = "Substitude";

        //Initialisieren des Parsers
        XmlPullParser parser = Xml.newPullParser();
        //StringReaser deklarieren
        StringReader stream = null;
        //Loginfo ausgeben (Dient nur zum Debug)
        Log.i("Debug", "Stream initialisiert");
        try {
            //StreamReader initialisieren
            stream = new StringReader(response);
            //Loginfo ausgeben (Dient nur zum Debug)
            Log.i("Debug", "Stream eingelesen");
            //Parser den Inputstream übergeben
            parser.setInput(stream);
            //Loginfo ausgeben (Dient nur zum Debug)
            Log.i("Debug", "Parser gesetzt");
            //Speichert, um welches Element es sich im XML-Code handelt
            int eventType = parser.getEventType();
            //Bool, die zeigt ob der Code fertig geparst wurde
            boolean done = false;
            //Leere Lesson erzeugen zum zichenspeichern der Werte
            Lesson cacheLesson = null;
            //Loginfo ausgeben (Dient nur zum Debug)
            Log.i("Debug", "Leere Lesson erzeugt");
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                //Varriabele für den Name des aktuellen XML-Tags deklarieren
                String name;
                //Prüfen um welchen EventTyp es sich handelt
                switch (eventType){
                    //Wenn das Dokument startet ...
                    case XmlPullParser.START_DOCUMENT:
                        //Loginfo ausgeben (Dient nur zum Debug)
                        Log.i("Debug", "Start festgestellt");
                        //Nichts auführen und zum nächsten Tag übergehen
                        break;
                    //Wenn ein Tag startet
                    case XmlPullParser.START_TAG:
                        //Namen des Tags speichern
                        name = parser.getName();
                        //Loginfo ausgeben (Dient nur zum Debug)
                        Log.i("Debug", name + " - Tag festgestellt");
                        //Prüfen ob es sich um den XML-Tag einer neuen Schulstunde handelt
                        if (name.equalsIgnoreCase(lesson)) {
                            //Neue Stunde wird gespeichert
                            cacheLesson = new Lesson();
                        }
                        //Speichern des Datums, wann die letzte aktualisierung stattfand
                        else if (name.equalsIgnoreCase(refreshed)){
                            Lesson.REFRESHED = parser.nextText();
                            Log.i("Refreshed", Lesson.REFRESHED);
                        }
                        else if (cacheLesson != null){
                            //Der XML-Tag ist der Tag
                            if (name.equalsIgnoreCase(tag)){
                                //Speichern des Tages in der Lesson Eigenschaft
                                cacheLesson.TAG = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist die Position
                            else if (name.equalsIgnoreCase(pos)){
                                //Speichern der Position in der Lesson Eigenschaft
                                cacheLesson.POS = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist der Lehrer
                            else if (name.equalsIgnoreCase(lehrer)){
                                //Speichern des Lehrers in der Lesson Eigenschaft
                                cacheLesson.LEHRER = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist das Fach
                            else if (name.equalsIgnoreCase(fach)){
                                //Speichern des Fachs in der Lesson Eigenschaft
                                cacheLesson.FACH = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist der Raum
                            else if (name.equalsIgnoreCase(raum)){
                                //Speichern des Raumes in der Lesson Eigenschaft
                                cacheLesson.RAUM = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist die Klasse
                            else if (name.equalsIgnoreCase(klasse)){
                                //Speichern der Klasse in der Lesson Eigenschaft
                                cacheLesson.KLASSE = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist der Vertreter
                            else if (name.equalsIgnoreCase(vertreter)){
                                //Speichern des Vertreters in der Lesson Eigenschaft
                                cacheLesson.VERTRETER = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist die Art der Vertretung
                            else if (name.equalsIgnoreCase(art)){
                                //Speichern der Art in der Lesson Eigenschaft
                                cacheLesson.ART = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                            //Der XML-Tag ist die Zusatzinfo
                            else if (name.equalsIgnoreCase(info)){
                                //Speichern der Info in der Lesson Eigenschaft
                                cacheLesson.INFO = parser.nextText().replace('\n', '\0').replace('\r', '\0');
                            }
                        }
                        break;
                    //Wenn der XML-Tag das Ende des Dokuments bedeutet
                    case XmlPullParser.END_TAG:
                        //Speichern des XML-Tag Namen
                        name = parser.getName();
                        if (name.equalsIgnoreCase(lesson)){
                            //Loginfo ausgeben (Dient nur zum Debug)
                            Log.i("Debug", "Lesson Added");
                            //Hinzufügen der zwichengespeicherten Stunde zu der Liste der Schulstunden
                            Lesson.LESSONS.add(cacheLesson);
                        //Sonst, wenn das Ende des Dokuments erreicht ist wird "done" auf true gesetzt
                        } else if (name.equalsIgnoreCase(document)){
                            done = true;
                        }
                        break;
                }
                //Nächter EventType wird geladen (Nächster XML-Tag wird eingelesen)
                eventType = parser.next();
            }

        }
        //Wenn ein Fehler auftritt
        catch (Exception e) {
            //Den Fehler ins Log schreiben
            throw new RuntimeException(e);
        }
        //Ohnehin
        finally {
            //Prüfen ob Stream initialisiert ist
            if (stream != null) {
                try {
                    //Versuchen den Stream zu schließen
                    stream.close();
                }
                catch (Exception e) {
                    //Im Log den Fehler ausgeben
                    e.printStackTrace();
                }
            }
        }

        //An diesem Punkt sind Schulstunden befüllt
        //Inhalt in Liste Lesson.LESSONS

        //Prüfen, ob ein Erbegnis vorhanden ist
        if (Lesson.LESSONS.size() < 1){
            //Ausgabe der Meldung
            Toast toast = Toast.makeText(getApplicationContext(), R.string.noLessons, Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        //An dieser Stelle ist sicher, dass es Stunden gibt

        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.lessons1) + Lesson.LESSONS.size() + getString(R.string.lessons2), Toast.LENGTH_LONG);
        toast.show();

        //Initialisieren und deklarieren der expandablelistview
        //Quelle: https://www.youtube.com/watch?v=0FJUwpnjScQ && https://www.youtube.com/watch?v=upYp631sffc (27.02.2018)
        final ExpandableListView expandableListView = findViewById(R.id.lstvResult);
        //Initialisieren des Datenadapters
        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(MainActivity.this);

        //Zuweisen das Daten-Adapters
        expandableListView.setAdapter(adapter);

        //Setzten des onClickListeners, damit nur eine Gruppe gleichzeitig geöfnet ist
        //Quelle: https://stackoverflow.com/questions/17586174/collapse-all-group-except-selected-group-in-expandable-listview-android (27.02.2018)
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //Prüfen ob schon eine Gruppe geöffnet wurde und wenn, ob diese die gleiche ist, die gerade geöffnet wurde
                if (lastExpanded != -1 && groupPosition != lastExpanded)
                    //Einklappen der Gruppe
                    expandableListView.collapseGroup(lastExpanded);
                //Speichern des neuen Indexes der offenen Gruppe
                lastExpanded = groupPosition;
            }
        });


    }

    /**
     * Testet, ob das gespeicherte Passwort korrekt ist
     * @param pass  zu Prüfendes Passwort
     * @param queue Volley queue, mit der die Anfrage ausgeführt werden soll
     */
    public void testCredentials(String pass, RequestQueue queue){
        //URL wird generiert
        final String url = "https://eskes.de/janmarcus/Server/index.php?check=" + pass;

        //Erstellen der Volley Anfrage
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Initialisieren der Steuerelemente
                Button btnSearch = findViewById(R.id.btnSearch);
                //Passwort ist nicht korrekt
                if (!response.contains("true")){
                    //Fehlermeldung, dass die Anmeldedaten erneut eingegeben werden sollen
                    Toast toast = Toast.makeText(getApplicationContext(),R.string.loginWrong, Toast.LENGTH_LONG);
                    toast.show();
                    //btnSearch wird deaktiviert, da sonst kein korrektes Passwort übergeben werden kann
                    btnSearch.setEnabled(false);
                }
                //Passwort ist korrekt
                else
                    //btnSearch wird wieder aktiviert
                    btnSearch.setEnabled(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Fehlermeldung, da der Server nicht antwortet
                Toast toast = Toast.makeText(getApplicationContext(),R.string.downloadFail, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        //Ausführen der Volley Anfrage
        queue.add(stringRequest);
    }

    /**
     * Wird zum hinterlegen der Menüs genutzt und automatisch angesprochen
     * Quelle: https://developer.android.com/training/appbar/actions.html
     * @param menu  Das Menü, welches hinterlegt werden soll
     * @return  War das hinzufügen erfolgreich oder nicht
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Verknüfen der Menü-Resource und dem Parameter menu
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Löst aus, wenn ein Item des Menüs ausgewählt wurde
     * Quelle: https://developer.android.com/training/appbar/actions.html
     * @param item  Das Item, dass das Event ausgelöst hat
     * @return  Ob die ausführung erfolgreich war
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //btnSettings hat das Event ausgelöst
            case R.id.btnSettings:
                //Settings-Activity wird gestartet
                //Quellen: https://developer.android.com/training/basics/firstapp/starting-activity.html , https://stackoverflow.com/questions/4186021/how-to-start-new-activity-on-button-click (27.02.2018)
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            //btnClose hat das Event ausgelöst
            case R.id.btnClose:
                //Die Anwendung wird geschlossen (leert RAM-Bereich und Beendet sich komplett)
                //Quelle: https://stackoverflow.com/questions/2092951/how-to-close-android-application (27.02.2018)
                System.exit(0);
                return true;
        }
        //Wenn kein Item zutrifft, durchsuchen der Elternklasse nach Item
        return super.onOptionsItemSelected(item);
    }
}