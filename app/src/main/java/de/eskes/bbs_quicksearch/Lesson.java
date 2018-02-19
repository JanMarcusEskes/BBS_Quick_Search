package de.eskes.bbs_quicksearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Dient zum Parsen des XML Codes ind Lesson Objekte, die dann in Listen gespeichert werden und durch die eigenen Methoden in andere Objekte konvertiert
 * werden können. Beispielsweise in ListView Itmes etc.
 */

public class Lesson {
    public String TAG;
    public String POS;
    public String LEHRER;
    public String FACH;
    public String RAUM;
    public String KLASSE;
    public String VERTRETER;
    public String ART;
    public String INFO;
    public static List<Lesson> LESSONS = new ArrayList<>();

    public String toString(){
        return TAG + ";" + POS + ";" + LEHRER + ";" + FACH + ";" + RAUM + ";" + KLASSE  + ";" + VERTRETER  + ";" + ART + ";" + INFO;
    }

}
