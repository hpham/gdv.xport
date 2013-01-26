/*
 * Copyright (c) 2011, 2012 by Oli B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 23.03.2011 by Oli B. (ob@aosd.de)
 */

package gdv.xport.satz.feld.sparte10.wagnisart5;

import gdv.xport.annotation.FeldInfo;
import gdv.xport.annotation.FelderInfo;
import gdv.xport.feld.AlphaNumFeld;
import gdv.xport.feld.NumFeld;
import gdv.xport.feld.Zeichen;
import gdv.xport.satz.feld.common.Feld1bis7;

/**
 * Diese Enum-Klasse repraesentiert die Felder fuer Satzart 220, Sparte 10 <br/>
 * "Leben - Risikozusatz = Wagnisart 5 - Bezugsrechte" (Satzart 0220)
 * 
 * @author ralfklemmer
 * @since 19.01.2013
 */
public enum Feld220Wagnis5Bezugsrechte {

    // /// Teildatensatz 1 /////////////////////////////////////////////////

    /** Feld 1 - 7 sind fuer jeden (Teil-)Datensatz identisch. */
    @FelderInfo(teildatensatz = 1, type = Feld1bis7.class)
    INTRO1,

    /**
     * Lfd. Nummer der versicherten Person (VP).<br/>
     * lfd. Nr., die im VU geführt wird
     */
    @FeldInfo(teildatensatz = 1, nr = 8, type = AlphaNumFeld.class, anzahlBytes = 17, byteAdresse = 43)
    LFD_NUMMER_VP_PERSONENGRUPPE,

    /**
     * Wagnisart.<br/>
     * 6 = Unfallzusatzversicherung
     */
    @FeldInfo(teildatensatz = 1, nr = 9, type = Zeichen.class, anzahlBytes = 1, byteAdresse = 60)
    WAGNISART,

    /**
     * Lfd Nummer zur Wagnisart.<br/>
     */
    @FeldInfo(teildatensatz = 1, nr = 10, type = Zeichen.class, anzahlBytes = 1, byteAdresse = 61)
    LFD_NUMMER_ZUR_WAGNISART,

    /**
     * Lfd. Nummer der Satzart<br/>
     * Lfd. Nummer der Satzart 0220.010.5/6 innerhalb der gleichen Folgenummer<br/>
     * (z. B. n-fache hintereinanderfolgende Lieferung der Satzart 0220.010.5/6, wenn mehrere Bezugsrechte vorhanden)
     */
    @FeldInfo(teildatensatz = 1, nr = 11, type = AlphaNumFeld.class, anzahlBytes = 2, byteAdresse = 62)
    LFD_NUMMER_DER_SATZART,

    /**
     * Bezugsberechtigt im Leistungsfall<br/>
     * 1 = Versicherungsnehmer = gesetzliche Erbfolge<br/>
     * 3 = Reihenfolge: Ehegatte, Kinder, Eltern, Erben<br/>
     * 9 = Sonstiger Bezugsberechtigter
     */
    @FeldInfo(teildatensatz = 1, nr = 12, type = Zeichen.class, anzahlBytes = 1, byteAdresse = 64)
    BEZUGSBERECHTIGT_IM_TODESFALL,

    /**
     * Sonstiger Bezugsberechtigter im Todessfall<br/>
     * Klartext (z. B. Name, Vorname)
     */
    @FeldInfo(teildatensatz = 1, nr = 13, type = AlphaNumFeld.class, anzahlBytes = 30, byteAdresse = 65)
    SONSTIGER_BEZUGSBERECHTIGTER_IM_TODESFALL,

    /**
     * Bezugsrechtanteil im Todesfall<br/>
     * in Prozent (3,2 Stellen)
     */
    @FeldInfo(teildatensatz = 1, nr = 14, type = NumFeld.class, anzahlBytes = 5, byteAdresse = 95)
    BEZUGSRECHTANTEIL_IM_TODESFALL,

    /**
     * Unwiderrufliches Bezugsrecht im Todesfall<br/>
     * 0 = nein, 1 = ja
     */
    @FeldInfo(teildatensatz = 1, nr = 15, type = Zeichen.class, anzahlBytes = 1, byteAdresse = 100)
    UNWIDERRUFLICHES_BEZUGSRECHT_IM_TODESFALL,

    /**
     * Leerstellen.<br/>
     */
    @FeldInfo(teildatensatz = 1, nr = 16, type = AlphaNumFeld.class, anzahlBytes = 155, byteAdresse = 101)
    LEERSTELLEN
}
