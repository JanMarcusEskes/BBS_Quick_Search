package de.eskes.bbs_quicksearch;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SettingsActivity extends AppCompatActivity {
    private static boolean CREDENTIALS = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Laden der gespeicherten Einstellungen
        SharedPreferences settings =    getSharedPreferences("Settings", 0);
        //Initialisieren des Download Assistenten
        final RequestQueue queue =      Volley.newRequestQueue(this);
        //Setzen der "CREDENTIALS" Variabele, so das "btnLogin" deaktiviert wird
        setCredentials(settings.getBoolean("Credentials", false));

        //Initialisieren der Steuerelemente
        final SeekBar seekBar =         findViewById(R.id.skbDays);
        final TextView seekBarValue =   findViewById(R.id.lblDaysValue);
        final EditText txtUser =        findViewById(R.id.txtUser);
        final EditText txtPass =        findViewById(R.id.txtPass);
        final Button btnLogin =         findViewById(R.id.btnSettings);
        final Button btnSave =          findViewById(R.id.btnSave);

        //Setzen der Werte anhand der Einstellungen
        txtUser.setText(settings.getString("User", "info"));
        txtPass.setText(settings.getString("Pass", ""));
        seekBar.setProgress(settings.getInt("Days", 6));
        seekBarValue.setText(getString(R.string.sliderDaysValue) + String.valueOf(seekBar.getProgress()));

        //OnChange Event abfangen
        //Quelle: https://developer.android.com/reference/android/widget/SeekBar.OnSeekBarChangeListener.html (27.02.2018)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Wenn der Wert sich ändert den Text von "lblDaysValue" ändern
                seekBarValue.setText(getString(R.string.sliderDaysValue) + String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //OnClick Event abfangen
        btnLogin.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v){
                //Wenn der Button "btnLogin" gedrückt wird werden die eingegebenen Daten überprüft
                //Der "queue" wird das StringRequest-Objekt übergeben, welches dann von ihr ausgeführt wird
                queue.add(testCredentials(txtPass.getText().toString()));
            }
        });

        //OnClick Event abfangen
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Speichern der Einstellungen
                saveSettings();
                //Schließen der "Settings"-Activity
                onBackPressed();
            }
        });

        //TextChange Event abfangen
        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Wenn sich das eingegebene Passwort verändert wird der Button wieder aktiviert
                setCredentials(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Generiert eine "StringRequest", die das Passwort, welches als Parameter übergeben wird prüft
     * Quelle: Siehe MainActivity
     * @param pass Das Passwort, das überprüft werden soll
     * @return Die fertige "StringRequest"
     */
    private StringRequest testCredentials(String pass){
        //URL, an die die Anfrage geschickt werden soll
        final String url = "https://eskes.de/janmarcus/Server/index.php?check=" + pass;

        //StringRequest, die zurückgegeben wird

        //Zurückgeben der StringRequest
        return new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Der Server läuft mit einem slebstgeschriebenen PHP-Script, welches beim Parameter "check" das mit übergebene Passwort prüft und mit "true" oder "false" antwortet
                if (response.contains("true")){
                    //Toasts sind ähnlich wie eine MessageBox bei .NET Programmierungen
                    //Verweis auf die "strings.xml", was zur Folge hat, dass die Übersetzten Versionen eingesetzt werden
                    Toast toast = Toast.makeText(getApplicationContext(),R.string.loginSuccessful, Toast.LENGTH_LONG);
                    //Anzeigen des Toast
                    toast.show();
                    //Setzen von "CREDENTIALS"
                    setCredentials(true);
                }
                else{
                    //Anmeldung am Server ist fehlgeschlagen
                    Toast toast = Toast.makeText(getApplicationContext(),R.string.loginFailed, Toast.LENGTH_LONG);
                    toast.show();
                    setCredentials(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Fehlermeldung, dass der Download nicht funktioniert hat
                Toast toast = Toast.makeText(getApplicationContext(),R.string.downloadFail, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * Setzt die boolen "CREDENTIALS" und de-/aktiviert den Button "btnLogin"
     * @param value Wert, der für "CREDENTIALS" gesetzt werden soll (true = Login erfolgreich)
     */
    private void setCredentials(boolean value){
        //Setzten von "CREDENTIALS"
        CREDENTIALS = value;
        //Initialisieren des Buttons
        Button btnLogin = findViewById(R.id.btnSettings);
        //de-/aktivieren des Buttons
        btnLogin.setEnabled(!value);
    }

    /**
     * Dient zum speichern der gesetzten Einstellungen
     * Quelle: Siehe MainActivity
     */
    private void saveSettings(){
        //Laden der gespeicherten Einstellungen
        SharedPreferences settings = getSharedPreferences("Settings", 0);
        //Initialisieren des "Editor", ohne den keine Einstellungen geändert werden können
        SharedPreferences.Editor editor = settings.edit();

        //Initialisieren der Steuerelemente
        SeekBar seekBar = findViewById(R.id.skbDays);
        EditText txtUser = findViewById(R.id.txtUser);
        EditText txtPass = findViewById(R.id.txtPass);

        //Speichern der Anzahl der Tage, die durchsucht werden sollen
        editor.putInt("Days", seekBar.getProgress());

        //Wenn die eingegebenen Anmeldedaten korrekt sind werden diese auch gespeichert, sonst nicht
        if (CREDENTIALS) {
            //Speichern der Anmeldedaten
            //noinspection ConstantConditions
            editor.putBoolean("Credentials", CREDENTIALS);
            editor.putString("User", txtUser.getText().toString());
            editor.putString("Pass", txtPass.getText().toString());
        }
        else {
            //Medung, dass die Anmeldedaten nicht gespeichert wurden
            Toast toast = Toast.makeText(getApplicationContext(), R.string.credentialsNotSaved, Toast.LENGTH_SHORT);
            toast.show();
        }
        //Der "Editor" speichert hier die Änderungen und schließt sich danach
        editor.apply();
    }

    /**
     * Quelle: https://developer.android.com/guide/components/activities/activity-lifecycle.html (27.02.2018)
     */
    @Override
    protected void onStop(){
        super.onStop();
        //Speichern der Einstellungen
        saveSettings();
    }
}
