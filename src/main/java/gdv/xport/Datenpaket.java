/*
 * Copyright (c) 2009 by agentes
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
 * (c)reated 23.10.2009 by Oli B. (oliver.boehm@agentes.de)
 */

package gdv.xport;

import static gdv.xport.feld.Bezeichner.*;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;

import gdv.xport.config.Config;
import gdv.xport.feld.*;
import gdv.xport.satz.*;

/**
 * @author oliver
 * @since 23.10.2009
 * @version $Revision$
 *
 */
public final class Datenpaket {
	
	private static final Log log = LogFactory.getLog(Datenpaket.class);
	private final Vorsatz vorsatz = new Vorsatz();
	private List<Datensatz> datensaetze = new ArrayList<Datensatz>();
	private final Nachsatz nachsatz = new Nachsatz();
	
	public Datenpaket() {
		Datum heute = Datum.heute();
		this.setErstellungsDatumVon(heute);
		this.setErstellungsDatumBis(heute);
		this.setAbsender(Config.getVUNummer().getInhalt());
	}
	
	/**
	 * @return the datensaetze
	 */
	public List<Datensatz> getDatensaetze() {
		return datensaetze;
	}

	/**
	 * @param datensaetze the datensaetze to set
	 */
	public void setDatensaetze(List<Datensatz> datensaetze) {
		this.datensaetze = datensaetze;
	}

	/**
	 * @return the vorsatz
	 */
	public Vorsatz getVorsatz() {
		return vorsatz;
	}

	/**
	 * @return the nachsatz
	 */
	public Nachsatz getNachsatz() {
		return nachsatz;
	}

	public void add(Datensatz datensatz) {
		datensaetze.add(datensatz);
		nachsatz.increaseAnzahlSaetze();
		vorsatz.setVersionFor(datensatz);
	}
	
	public void export(File file) throws IOException {
		Writer writer = new FileWriter(file);
		try {
			export(writer);
		} finally {
			writer.close();
		}
	}
	
	public void export(Writer writer) throws IOException {
		vorsatz.export(writer);
		for (Iterator<Datensatz> iterator = datensaetze.iterator(); iterator.hasNext();) {
			Datensatz datensatz = iterator.next();
			datensatz.export(writer);
		}
		nachsatz.export(writer);
		log.info(datensaetze.size() + " Datensaetze exported.");
	}
	
	public void setErstellungsDatumVon(Datum d) {
		Datum von = this.getErstellungsDatumVon();
		von.setInhalt(d);
	}

	public Datum getErstellungsDatumVon() {
		return (Datum) this.vorsatz.getFeld(ERSTELLUNGSDATUM_ZEITRAUM_VOM);
	}
	
	public void setErstellungsDatumBis(Datum d) {
		Datum bis = this.getErstellungsDatumBis();
		bis.setInhalt(d);
	}

	public Datum getErstellungsDatumBis() {
		return (Datum) this.vorsatz.getFeld(ERSTELLUNGSDATUM_ZEITRAUM_BIS);
	}
	
	public void setAbsender(String s) {
		Feld absender = this.getAbsenderFeld();
		absender.setInhalt(s);
	}
	
	public String getAbsender() {
		return this.getAbsenderFeld().getInhalt().trim();
	}
	
	private Feld getAbsenderFeld() {
		return this.vorsatz.getFeld(ABSENDER);
	}
	
	public void setAdressat(String s) {
		Feld adressat = this.getAdressatFeld();
		adressat.setInhalt(s);
	}
	
	public String getAdressat() {
		return this.getAdressatFeld().getInhalt().trim();
	}
	
	private Feld getAdressatFeld() {
		return this.vorsatz.getFeld(ADRESSAT);
	}
	
	public void setVermittler(String s) {
		this.vorsatz.setVermittler(s);
		this.nachsatz.setVermittler(s);
	}
	
	public String getVermittler() {
		String vermittler = this.vorsatz.getVermittler();
		assert vermittler.equals(this.nachsatz.getVermittler()) : vorsatz
				+ " or " + nachsatz + " is corrupt";
		return vermittler;
	}

}
