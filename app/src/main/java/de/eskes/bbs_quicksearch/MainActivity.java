package de.eskes.bbs_quicksearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    private String SERVER_RESPONSE;

    /**
     * Löst aus, wenn die Activity wieder den Focus hat
     * Quelle: https://developer.android.com/guide/components/activities/activity-lifecycle.html
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
                //Initialisieren des Editor zum schreiben der Einstellungen
                //Quelle: https://developer.android.com/reference/android/content/SharedPreferences.Editor.html
                SharedPreferences.Editor editor = settings.edit();
                //Speicher des zuletzt eingegebenen Suchbegriffes
                editor.putString("Term", txtSearchterm.getText().toString());
                //Anwenden der Änderungen
                editor.commit();

                //URL an die die Anfrage geschickt wird generieren
                String url = "https://eskes.de/janmarcus/Server/index.php?output=XML2&password=" + settings.getString("Pass","") + "&search=" + txtSearchterm.getText().toString() + "&sites=" + settings.getInt("Days", 5);

                //Zu Debug Zwecken die URL ausgeben
                Toast toast = Toast.makeText(getApplicationContext(),url, Toast.LENGTH_LONG);
                toast.show();

                //Erstellen der Volley Anfrage
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //speichern der Antwort des Servers
                    SERVER_RESPONSE = response;
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

                //TODO: Implement search Logic
                //Die Antwort ist in SERVER_RESPONSE gespeichert

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
        return;
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
                //Quellen: https://developer.android.com/training/basics/firstapp/starting-activity.html , https://stackoverflow.com/questions/4186021/how-to-start-new-activity-on-button-click
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            //btnClose hat das Event ausgelöst
            case R.id.btnClose:
                //Die Anwendung wird geschlossen (leert RAM-Bereich und Beendet sich komplett)
                //Quelle: https://stackoverflow.com/questions/2092951/how-to-close-android-application
                System.exit(0);
                return true;
        }
        //Wenn kein Item zutrifft, durchsuchen der Elternklasse nach Item
        return super.onOptionsItemSelected(item);
    }
}