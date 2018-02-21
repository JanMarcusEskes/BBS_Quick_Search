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

    //Statische Liste, zum Speichern aller Stunden
    public static List<Lesson> LESSONS = new ArrayList<>();

    //Darstellen der Eigenschaften in einem String
    public String toString(){
        return "Tag : " + TAG + "\n\nStunde : " + POS + "\n\nLehrer : " + LEHRER + "\n\nFach : " + FACH + "\n\nRaum : " + RAUM + "\n\nKlasse : " + KLASSE  + "\n\nVertreter : " + VERTRETER  + "\n\nArt : " + ART + "\n\nInfo : " + INFO;
    }

    //Generiert die entsprechende Gruppenüberschrift (Für ExpandableListViewAdapter)
    public String getGroupText(){
        return TAG + " " + KLASSE;
    }

    //Generiert die entsprechende Childüberschrift (Für ExpandableListViewAdapter)
    public String getChildText(){
        String zusatz;
        if(ART.contains("Frei"))
            zusatz = ART;
        else if(ART.contains("verschoben"))
            zusatz = "Verschoben";
        else if(ART.contains("Stillarbeit"))
            zusatz = "Stillarbeit";
        else if(ART.contains("Raum"))
            zusatz = RAUM;
        else if(!VERTRETER.equalsIgnoreCase(""))
            zusatz = "Vertretung";
        else
            zusatz = "Anderes";

        return POS + " Stunde\t\t" + FACH + "\t\t" + zusatz;
    }

}
