/**
 *
 */
package gdv.xport.satz;

import static gdv.xport.feld.Bezeichner.NAME_SATZART;
import static gdv.xport.feld.Bezeichner.NAME_SPARTE;
import static patterntesting.runtime.NullConstants.NULL_STRING;
import gdv.xport.annotation.FeldInfo;
import gdv.xport.annotation.FelderInfo;
import gdv.xport.config.Config;
import gdv.xport.feld.Bezeichner;
import gdv.xport.feld.Feld;
import gdv.xport.feld.NumFeld;
import gdv.xport.io.ImportException;
import gdv.xport.io.PushbackLineNumberReader;
import gdv.xport.satz.feld.MetaFeldInfo;
import gdv.xport.satz.feld.common.Feld1bis7;
import gdv.xport.util.SatzNummer;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertCheck;
import net.sf.oval.context.ClassContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Die Satz-Klasse ist die oberste Klasse, von der alle weiteren Saetze
 * abgeleitet sind.
 *
 * @author oliver
 */
public abstract class Satz {

	private static final Logger LOG = LoggerFactory.getLogger(Satz.class);
	private final NumFeld satzart = new NumFeld(new Bezeichner(NAME_SATZART), 4, 1);
	private Teildatensatz[] teildatensatz = new Teildatensatz[0];

	protected Satz(final int art) {
		this(art, 1);
	}

	protected Satz(final String art) {
		this(art, (art.length() + 255) / 256);
	}

	protected Satz(final NumFeld art) {
		this(art.getInhalt());
	}

	/**
	 * Instantiates a new satz.
	 *
	 * @param art Satzart
	 * @param n Anzahl der Teildatensaetze
	 */
	public Satz(final NumFeld art, final int n) {
		this.satzart.setInhalt(art.getInhalt());
		this.createTeildatensaetze(n);
	}

	/**
	 * Instantiates a new satz.
	 *
	 * @param content the content
	 * @param n the n
	 */
	public Satz(final String content, final int n) {
		this.satzart.setInhalt(content);
		this.createTeildatensaetze(n);
		if (content.length() > 4) {
			try {
				this.importFrom(content);
			} catch (IOException ioe) {
				throw new IllegalArgumentException("1st argument too short", ioe);
			}
		}
	}

	/**
	 * The Constructor.
	 *
	 * @param art z.B. 100 (f. Adressteil)
	 * @param n Anzahl der Teildatensaetze
	 */
	public Satz(final int art, final int n) {
		this.satzart.setInhalt(art);
		this.createTeildatensaetze(n);
	}

	/**
	 * Instanziiert einen neuen Satz.
	 *
	 * @param art z.B. 100 (f. Adressteil)
	 * @param tdsList Liste mit den Teildatensaetzen
	 */
	public Satz(final int art, final List<? extends Teildatensatz> tdsList) {
		this.satzart.setInhalt(art);
		this.createTeildatensaetze(tdsList);
	}

	protected void createTeildatensaetze(final int n) {
		teildatensatz = new Teildatensatz[n];
		for (int i = 0; i < n; i++) {
			teildatensatz[i] = new Teildatensatz(satzart, i + 1);
		}
	}

	protected void createTeildatensaetze(final List<? extends Teildatensatz> tdsList) {
		teildatensatz = new Teildatensatz[tdsList.size()];
		for (int i = 0; i < tdsList.size(); i++) {
			teildatensatz[i] = tdsList.get(i);
			teildatensatz[i].add(this.satzart);
		}
	}

	/**
	 * Liefert alle Teildatensaetze zurueck.
	 *
	 * @return Teildatensaetze
	 * @since 0.2
	 */
	public final Collection<Teildatensatz> getTeildatensaetze() {
		return Arrays.asList(this.teildatensatz);
	}

	/**
	 * Liefert die Anzahl der Teildatensaetze.
	 *
	 * @return Anzahl der Teildatensaetze.
	 * @since 0.6
	 */
	public final int getNumberOfTeildatensaetze() {
		return teildatensatz.length;
	}

	/**
	 * Liefert den n-ten Teildatensatz zurueck.
	 *
	 * @param n Nummer des Teildatensatzes (beginnend mit 1)
	 * @return the teildatensatz
	 * @since 0.2
	 */
	public final Teildatensatz getTeildatensatz(final int n) {
		return this.teildatensatz[n - 1];
	}

	/**
	 * Hiermit koennen Unterklassen alle Teildatensaetze wieder entfernen (wird
	 * z.B. vom Satz 0220.030 benoetigt).
	 *
	 * @since 0.4
	 */
	public final void removeAllTeildatensaetze() {
		this.teildatensatz = new Teildatensatz[0];
	}

	/**
	 * Entfernt alle Teildatensaetze >= n.
	 *
	 * @since 0.4
	 * @param n ab hier wird abgeschnitten (n >= 1)
	 */
	public final void removeAllTeildatensaetze(final int n) {
		if ((n < 1) || (n > this.teildatensatz.length)) {
			throw new IllegalArgumentException(n + " liegt nicht zwischen 1 und "
			        + this.teildatensatz.length);
		}
		this.teildatensatz = (Teildatensatz[]) ArrayUtils.subarray(this.teildatensatz, 0, n - 1);
	}

	/**
	 * Entfernt den gewuenschten Teildatensatz. Ein neuer Teildatensatz kann
	 * ueber add() hinzugefuegt werden.
	 *
	 * @see #add(Teildatensatz)
	 * @since 0.4
	 * @param n der gewuenschte Teildatensatz (beginnend bei 1)
	 */
	public final void removeTeildatensatz(final int n) {
		if ((n < 1) || (n > this.teildatensatz.length)) {
			throw new IllegalArgumentException(n + " liegt nicht zwischen 1 und "
			        + this.teildatensatz.length);
		}
		this.teildatensatz = (Teildatensatz[]) ArrayUtils.remove(this.teildatensatz, n - 1);
	}

	/**
	 * Und hierueber kann ein Teildatensatz hinzugefuegt werden.
	 *
	 * @since 0.4
	 * @param tds der neue (gefuellte) Teildatensatz
	 */
	public final void add(final Teildatensatz tds) {
		this.teildatensatz = (Teildatensatz[]) ArrayUtils.add(this.teildatensatz, tds);
	}

	/**
	 * Fuegt das uebergebene Feld zur Liste der Datenfelder hinzu.
	 *
	 * @param feld das Feld
	 */
	public void add(final Feld feld) {
		this.add(feld, 1);
	}

	/**
	 * Fuegt das uebergebene Feld zur Liste der Datenfelder hinzu.
	 *
	 * @param feld the feld
	 * @param teildatensatzNr the teildatensatz nr
	 */
	public void add(final Feld feld, final int teildatensatzNr) {
		if (feld.getByteAdresse() > 256) {
			throw new IllegalArgumentException(feld + " ueberschreitet Teildatensatz-Grenze");
		}
		if ((teildatensatzNr < 1) || (teildatensatzNr > this.teildatensatz.length)) {
			throw new IllegalArgumentException("Teildatensatz-Nr. " + teildatensatzNr + " fuer "
			        + feld + " liegt nicht zwischen 1 und " + teildatensatz.length);
		}
		this.teildatensatz[teildatensatzNr - 1].add(feld);
	}

	/**
	 * Fuellt fuer alle leeren Stellen ein entsprechendes Fuellfeld auf.
	 */
	public void addFiller() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Falls ein Feld zuviel gesetzt wurde, kann es mit 'remove" wieder entfernt
	 * werden.
	 *
	 * @param name Name des Feldes
	 */
	public void remove(final String name) {
		this.remove(new Bezeichner(name));
	}

    /**
     * Falls ein Feld zuviel gesetzt wurde, kann es mit 'remove" wieder entfernt
     * werden.
     *
     * @param bezeichner der Feld-Beezeichner
     * @since 1.0
     */
    public void remove(final Bezeichner bezeichner) {
        for (int i = 0; i < this.teildatensatz.length; i++) {
            this.teildatensatz[i].remove(bezeichner);
        }
    }

	/**
	 * Setzt das angegebene Feld in allen Teildatensaetzen, in denen es gefunden
	 * wird. Normalerweise braeuchten wir eigentlich nur die erste Fundstelle
	 * setzen, da die anderen Teildatensaetze (hoffentlich) auf die gleiche
	 * Referenz verweisen - aber sicher ist sicher. Falls das Feld nicht
	 * gefunden wird, wird eine IllegalArgumentException geworfen.
	 *
	 * @param name Name des Felds (Bezeichnung)
	 * @param value the value
	 */
	public void set(final String name, final String value) {
		boolean found = false;
		for (int i = 0; i < teildatensatz.length; i++) {
			Feld x = teildatensatz[i].getFeld(name);
			if (x != Feld.NULL_FELD) {
				x.setInhalt(value);
				found = true;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("Feld \"" + name + "\" not found");
		}
	}

	/**
	 * Setzt den Inhalt des gewuenschten Feldes.
	 *
	 * @param feldX das gewuenschte Feld-Element
	 * @param value neuer Inhalt
	 */
	public final void set(final Enum<?> feldX, final String value) {
		String name = Feld.getAsBezeichnung(feldX);
		this.set(name, value);
	}

    /**
     * Setzt den Inhalt des gewuenschten Feldes.
     *
     * @param feldX das gewuenschte Feld-Element
     * @param value neuer Inhalt
     * @since 0.9 (oboehm, 1-Apr-2013)
     */
    public final void set(final Enum<?> feldX, final Integer value) {
        this.set(feldX, Integer.toString(value));
    }

    /**
     * Setzt den Inhalt des gewuenschten Feldes.
     *
     * @param feldX das gewuenschte Feld-Element
     * @param value neuer Inhalt
     * @since 0.9 (oboehm, 1-Apr-2013)
     */
    public final void set(final Enum<?> feldX, final Character value) {
        this.set(feldX, Character.toString(value));
    }

	/**
	 * Liefert den Inhalt des gewuenschten Feldes.
	 *
	 * @param name gesuchtes Feld
	 * @return Inhalt des gefundenden Felds (NULL_STRING, falls 'name' nicht
	 * gefunden wurde)
	 */
	public final String get(final String name) {
		Feld f = getFeld(name);
		if (f == Feld.NULL_FELD) {
			return NULL_STRING;
		} else {
			return f.getInhalt();
		}
	}

	/**
	 * Liefert den Inhalt des gewuenschten Feldes.
	 *
	 * @param feldX das gewuenschte Feld-Element
	 * @return Inhalt des gefundenden Felds
	 */
	public final String get(final Enum<?> feldX) {
		String name = Feld.getAsBezeichnung(feldX);
		return this.get(name);
	}

	/**
	 * Liefert das gewuenschte Feld. Allerdings wird nur der Name des Feldes
	 * benutzt, um das Feld zu bestimmen. Dazu werden auch die Konstanten in
     * {@link gdv.xport.feld.Bezeichner} verwendet.
	 * <p>
	 * TODO: Eigentlich waere es sinnvoller, hier die restlichen Annotationen
	 * auszuwerten, da der Name nur auf Konvention beruht und etwas wackelig
	 * ist (oboehm, 1-Apr-2013).
	 * </p>
	 *
	 * @param feld gewuenschtes Feld-Element
	 * @return das gesuchte Feld
	 * @throws IllegalArgumentException falls es das Feld nicht gibt
	 */
	public Feld getFeld(final Enum<?> feld) throws IllegalArgumentException {
		for (int i = 0; i < teildatensatz.length; i++) {
			try {
				Feld x = teildatensatz[i].getFeld(feld);
				if (x != Feld.NULL_FELD) {
					return x;
				}
			} catch (IllegalArgumentException e) {
                LOG.debug("Feld \"" + feld + "\" not found in teildatensatz " + i + " (" + e + ").");
				continue;
			}
		}
		throw new IllegalArgumentException("Feld \"" + feld + "\" nicht in " + this.toShortString()
		        + " vorhanden!");
	}

	/**
	 * Liefert das gewuenschte Feld falls vorhanden oder null.
	 *
	 * @param name gewuenschter Bezeichner des Feldes
	 * @return das gesuchte Feld
	 */
	public Feld containsFeld(final String name) {
		for (int i = 0; i < teildatensatz.length; i++) {
			Feld x = teildatensatz[i].getFeld(name);
			if (x != Feld.NULL_FELD) {
				return x;
			}
		}
		LOG.debug("Feld \"{}\" not found in {}.", name, this);
		return null;
	}

	/**
	 * Liefert das gewuenschte Feld.
	 *
	 * @param name gewuenschter Bezeichner des Feldes
	 * @return das gesuchte Feld
	 * @throws IllegalArgumentException falls es das Feld nicht gibt
	 */
	public Feld getFeld(final String name) {
		return this.getFeld(new Bezeichner(name));
	}

    /**
     * Liefert das gewuenschte Feld.
     *
     * @param bezeichner gewuenschter Bezeichner des Feldes
     * @return das gesuchte Feld
     * @throws IllegalArgumentException falls es das Feld nicht gibt
     */
    public Feld getFeld(final Bezeichner bezeichner) {
        for (int i = 0; i < teildatensatz.length; i++) {
            Feld x = teildatensatz[i].getFeld(bezeichner);
            if (x != Feld.NULL_FELD) {
                return x;
            }
        }
        throw new IllegalArgumentException("Feld \"" + bezeichner + "\" nicht in " + this.toShortString()
                + " vorhanden!");
    }

	/**
	 * Liefert den Inhalt des gewuenschten Feldes.
	 *
	 * @since 0.3
	 * @param name gewuenschter Bezeichner des Feldes
	 * @return Inhalt des Feldes (getrimmt, d.h. ohne Leerzeichen am Ende)
	 */
	public final String getFeldInhalt(final String name) {
		return this.getFeld(name).getInhalt().trim();
	}

	/**
	 * Liefert das gewuenschte Feld.
	 *
	 * @param name gewuenschter Bezeichner des Feldes
	 * @param nr Nummer des Teildatensatzes (1, 2, ...)
	 * @return NULL_FELD, falls das angegebene Feld nicht gefunden wird
	 * @since 0.2
	 */
	public final Feld getFeld(final String name, final int nr) {
		assert (0 < nr) && (nr <= teildatensatz.length) : nr + " liegt ausserhalb des Bereichs";
		return teildatensatz[nr - 1].getFeld(name);
	}

	/**
	 * Liefert den Inhalt des gewuenschten Feldes.
	 *
	 * @param name gewuenschter Bezeichner des Feldes
	 * @param nr Nummer des Teildatensatzes (1, 2, ...)
	 * @return Inhalt des Feldes (getrimmt, d.h. ohne Leerzeichen am Ende)
	 * @since 0.3
	 */
	public final String getFeldInhalt(final String name, final int nr) {
		return this.getFeld(name, nr).getInhalt().trim();
	}

	/**
	 * Liefert die Satzart.
	 *
	 * @return the satzart
	 */
	public final NumFeld getSatzartFeld() {
		return this.satzart;
	}

	/**
	 * Liefert die Satzart zurueck.
	 *
	 * @return die Satzart als int
     * @since 0.3
	 */
	public final int getSatzart() {
		return this.satzart.toInt();
	}

	/**
	 * Liefert den Satz-Typ zurueck. Der Satz-Typ ist eine Zusammenfassung aus
	 * Satzart und Sparte.
	 *
	 * @return den Satz-Typ
	 * @since 1.0
	 */
	public final SatzNummer getSatzTyp() {
	    if (this.hasSparte()) {
	        return new SatzNummer(this.getSatzart(), this.getSparte());
	    } else {
	        return new SatzNummer(this.getSatzart());
	    }
	}

	/**
	 * Schaut nach einem Feld "SPARTE" und liefert true zurueck, falls es
	 * existiert.
	 *
	 * @since 0.9
	 * @return true, falls Sparten-Feld vorhanden ist
	 */
	public boolean hasSparte() {
	    Feld sparte = this.getFeld(Feld1bis7.SPARTE);
	    return (sparte != Feld.NULL_FELD) && !sparte.isEmpty();
	}

	/**
	 * Liefert den Inhalt des Sparten-Felds. Vorher sollte allerdings mittels
	 * {@link #hasSparte()} geprueft werden, ob der Satz ein Sparten-Feld
	 * besitzt.
	 *
     * @since 0.9
	 * @return die Sparte
	 */
	public int getSparte() {
	    NumFeld sparte = (NumFeld) this.getFeld(Feld1bis7.SPARTE);
	    return sparte.toInt();
	}

	/**
     * Exportiert den Satz.
	 *
	 * @param writer the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void export(final Writer writer) throws IOException {
		for (int i = 0; i < teildatensatz.length; i++) {
			teildatensatz[i].export(writer);
		}
	}

	/**
	 * Exportiert den Satz.
	 *
	 * @param file Datei
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void export(final File file) throws IOException {
	    Writer writer = new FileWriter(file);
	    try {
	        this.export(writer);
	    } finally {
	        writer.close();
	    }
	}

	/**
	 * Export.
	 *
	 * @param writer the writer
	 * @param eod das End-of-Datensatz- oder Trennzeichen (z.B. linefeed)
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void export(final Writer writer, final String eod) throws IOException {
		for (int i = 0; i < teildatensatz.length; i++) {
			teildatensatz[i].export(writer, eod);
		}
	}

	/**
	 * @since 0.3
	 * @param ostream z.B. System.out
	 * @throws IOException falls mal was schief geht
	 */
	public void export(final OutputStream ostream) throws IOException {
		Writer writer = new OutputStreamWriter(ostream);
		export(writer);
		writer.flush();
		ostream.flush();
	}

	/**
	 * Eigentlich wollte ich ja diese Methode "import" nennen, aber das
	 * kollidiert leider mit dem Schluesselwort "import" in Java. Inzwischen
	 * beruecksichtigt diese Import-Methode auch zusaetzlich eingestreute
	 * Newlines ("\n") oder/und Wagenruecklaeufe ("\r").
	 *
	 * @param s the s
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void importFrom(final String s) throws IOException {
		int satzlength = getSatzlength(s);
		for (int i = 0; i < teildatensatz.length; i++) {
			String input = s.substring(i * satzlength);
			if (input.trim().isEmpty()) {
				LOG.info("mehr Daten fuer Satz " + this.getSatzart() + " erwartet, aber nur " + i
				        + " Teildatensaetze vorgefunden");
				this.removeAllTeildatensaetze(i + 1);
				break;
			}
			teildatensatz[i].importFrom(input);
		}
	}

	/**
	 * Importiert einen Satz von der angegebenen Datei.
	 *
	 * @param file die Import-Datei
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void importFrom(final File file) throws IOException {
	    Reader reader = new FileReader(file);
	    try {
	        this.importFrom(reader);
	    } finally {
	        reader.close();
	    }
	}

	/**
	 * Ermittelt die Satzlaenge. Je nachdem, ob das Zeilenende aus keinem, einem
	 * oder zwei Zeichen besteht, wird 256, 257 oder 258 zurueckgegeben.
	 *
	 * @since 0.4
	 * @param s der komplette Satz
	 * @return 256, 257 oder 258
	 */
	protected final int getSatzlength(final String s) {
		int satzlength = 256;
		try {
			char c256 = s.charAt(256);
			if ((c256 == '\n') || (c256 == '\r')) {
				satzlength = 257;
			}
			if (s.length() > satzlength) {
				char c257 = s.charAt(257);
				if ((c257 == '\n') || (c257 == '\r')) {
					satzlength = 258;
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			LOG.trace("end of string \"" + s + "\" reached", e);
		}
		return satzlength;
	}

	/**
	 * Import from.
	 *
	 * @param istream the istream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void importFrom(final InputStream istream) throws IOException {
		importFrom(new InputStreamReader(istream, Config.DEFAULT_ENCODING));
	}

	/**
	 * Import from.
	 *
	 * @param reader the reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void importFrom(final Reader reader) throws IOException {
	    PushbackLineNumberReader lnr = new PushbackLineNumberReader(reader, 256);
		try {
            importFrom(lnr);
		} catch (IOException ioe) {
		    throw new ImportException(lnr, "read error", ioe);
		} catch (NumberFormatException nfe) {
            throw new ImportException(lnr, "number expected", nfe);
		}
	}

	/**
	 * Der hier verwendete PushbackReader wird benoetigt, damit die gelesene
	 * Satzart und Sparte wieder zurueckgesetllt werden kann. Seit 0.5.1 ist
	 * diese Methode nicht mehr final, da manche Satzarten wohl Eigenarten haben
	 * koennen (wie z.B. fehlende Sparten-Eintraege).
	 *
	 * @param reader the reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void importFrom(final PushbackLineNumberReader reader) throws IOException {
		char[] cbuf = new char[257 * teildatensatz.length];
		for (int i = 0; i < teildatensatz.length; i++) {
            reader.skipNewline();
			if (!matchesNextTeildatensatz(reader)) {
				LOG.info((teildatensatz.length - i) + " more Teildatensaetze expected for " + this
				        + ", but Satzart or Sparte or Wagnisart or TeildatensatzNummer has changed");
				break;
			}
			importFrom(reader, cbuf, i * 257);
			cbuf[i * 257 + 256] = '\n';
		}
		importFrom(new String(cbuf));
	}

	/**
	 * Unterklassen (wie Datensatz) sind dafuer verantwortlich, dass auch noch
	 * die Sparte ueberprueft wird, ob sie noch richtig ist oder ob da schon der
	 * naechste Satz beginnt. Hier (fuer den allgemeinen Fall) wird nur die
	 * Satzart ueberprueft.
	 *
	 * @param reader the reader
	 * @return true (Default-Implementierung)
	 * @throws IOException bei I/O-Fehlern
	 * @since 0.5.1
	 */
	protected boolean matchesNextTeildatensatz(final PushbackLineNumberReader reader) throws IOException {
		try {
            int art = readSatzart(reader);
            return art == this.getSatzart();
        } catch (EOFException ex) {
            LOG.info("No next teildatensatz:", ex);
            return false;
        }
	}

	private static void importFrom(final Reader reader, final char[] cbuf, final int i)
	        throws IOException {
		if (reader.read(cbuf, i, 256) == -1) {
			String s = new String(cbuf).substring(i);
			throw new IOException("can't read 256 bytes from " + reader + ", only " + s.length()
			        + " bytes: " + s);
		}
	}

	/**
	 * Liest 4 Bytes, um die Satzart zu bestimmen und stellt die Bytes
	 * anschliessend wieder zurueck in den Reader.
	 *
	 * @param reader the reader
	 * @return Satzart
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int readSatzart(final PushbackLineNumberReader reader) throws IOException {
        reader.skipWhitespace();
		char[] cbuf = new char[4];
		importFrom(reader, cbuf);
		reader.unread(cbuf);
        return Integer.parseInt(new String(cbuf));
	}

	private static void importFrom(final Reader reader, final char[] cbuf) throws IOException {
		if (reader.read(cbuf) == -1) {
			String s = new String(cbuf).trim();
			throw new EOFException("can't read " + cbuf.length + " bytes from " + reader + ", only \""
			        + s + "\" ("+ s.length() + " bytes)");
		}
	}

    /**
	 * Aus Performance-Gruenden stuetzt sich diese Methode nicht auf die
	 * validate()-Methode ab.
	 *
	 * @return true/false
	 */
	public boolean isValid() {
		if (!this.satzart.isValid()) {
			return false;
		}
		if (this.teildatensatz != null) {
			for (int i = 0; i < teildatensatz.length; i++) {
				if (!teildatensatz[i].isValid()) {
					LOG.info("Teildatensatz " + (i + 1) + " is invalid");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Validiert die einzelnen Teildatensaetze.
	 *
	 * @return the list< constraint violation>
	 */
	public List<ConstraintViolation> validate() {
		Validator validator = new Validator();
		List<ConstraintViolation> violations = validator.validate(this);
		if (!this.satzart.isValid()) {
			ConstraintViolation cv = new ConstraintViolation(new AssertCheck(), "invalid Satzart "
			        + this.satzart.getInhalt(), this, this.satzart, new ClassContext(
			        this.getClass()));
			violations.add(cv);
		}
		if (this.teildatensatz != null) {
			for (int i = 0; i < teildatensatz.length; i++) {
				violations.addAll(teildatensatz[i].validate());
			}
		}
		return violations;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		try {
            return this.toShortString() + " (" + StringUtils.abbreviate(this.toLongString(), 47) + ")";
		} catch (RuntimeException shouldNeverHappen) {
			LOG.error("shit happens in toString()", shouldNeverHappen);
			return super.toString();
		}
	}

	/**
	 * To short string.
	 *
	 * @return the string
	 */
	public String toShortString() {
		return "Satzart " + this.satzart.getInhalt();
	}

	/**
	 * To long string.
	 *
	 * @return the string
	 */
	public String toLongString() {
		StringWriter swriter = new StringWriter();
		try {
			this.export(swriter);
		} catch (IOException canthappen) {
			LOG.warn(canthappen + " ignored", canthappen);
			swriter.write(canthappen.getLocalizedMessage());
		}
		return swriter.toString();
	}

	/**
	 * Zwei Saetze sind gleich, wenn sie die gleichen Daten besitzen. Die
	 * Idee dabei ist, dass wir die beiden Saetze exportieren und dann das
	 * Resultat vergleichen.
	 *
	 * @param obj der andere Satz
	 * @return true, wenn beide Saetze gleich sind
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
	    if (!(obj instanceof Satz)) {
	        return false;
	    }
	    Satz other = (Satz) obj;
        return this.toLongString().equals(other.toLongString());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getSatzart();
	}

	// /// Enum-Behandlung und Auswertung der Meta-Infos ///////////////////

	/**
	 * Hier passiert die Magie: die Annotationen der uebergebenen Enum werden
	 * ausgelesen und in eine Liste mit den Teildatensaetzen gepackt.
	 *
	 * @param satzart the satzart
	 * @param felder the felder
	 * @return eine Liste mit Teildatensaetzen
	 */
	protected static List<Teildatensatz> getTeildatensaetzeFor(final int satzart,
	        final Enum<?>[] felder) {
		SortedMap<Integer, Teildatensatz> tdsMap = new TreeMap<Integer, Teildatensatz>();
		List<MetaFeldInfo> metaFeldInfos = getMetaFeldInfos(felder);
        for (MetaFeldInfo metaFeldInfo : metaFeldInfos) {
			int n = metaFeldInfo.getTeildatensatzNr();
			Teildatensatz tds = tdsMap.get(n);
			if (tds == null) {
				tds = new Teildatensatz(satzart, n);
				tdsMap.put(n, tds);
			}
			add(metaFeldInfo.getFeldEnum(), tds);

		}
        List<Teildatensatz> teildatensaetze = new ArrayList<Teildatensatz>(tdsMap.values());
		setSparteFor(teildatensaetze, metaFeldInfos);
		return teildatensaetze;
	}

    private static void setSparteFor(final List<Teildatensatz> teildatensaetze,
            final List<MetaFeldInfo> metaFeldInfos) {
        int sparte = getSparte(metaFeldInfos);
        if (sparte > 0) {
            setSparteFor(teildatensaetze, sparte);
        }
    }

    private static int getSparte(final List<MetaFeldInfo> metaFeldInfos) {
        for (MetaFeldInfo info : metaFeldInfos) {
            if (info.hasSparte()) {
                return info.getSparte();
            }
        }
        return -1;
    }

    private static void setSparteFor(final List<Teildatensatz> teildatensaetze, final int sparte) {
        for (Teildatensatz teildatensatz : teildatensaetze) {
            setSparteFor(teildatensatz, sparte);
        }
    }

    private static void setSparteFor(final Teildatensatz tds, final int sparte) {
        Feld spartenFeld = tds.getFeld(Feld1bis7.SPARTE);
        if (spartenFeld == Feld.NULL_FELD) {
            spartenFeld = new NumFeld(new Bezeichner(NAME_SPARTE), 3, 11);
            tds.add(spartenFeld);
        }
        spartenFeld.setInhalt(sparte);
    }

    /**
	 * Wandelt das uebergebene Array in eine Liste mit MetaFeldInfos. Seit 0.7.1
	 * duerfen Feld-Enums wie {@link gdv.xport.satz.feld.Feld100} auch
	 * FelderInfo-Annotationen enthalten, die wiederum auf einen Enum verweisen.
	 *
	 * @param felder the felder
	 * @return the meta feld infos
	 */
	protected static List<MetaFeldInfo> getMetaFeldInfos(final Enum<?>[] felder) {
		List<MetaFeldInfo> metaFeldInfos = new ArrayList<MetaFeldInfo>(felder.length);
		for (int i = 0; i < felder.length; i++) {
			String name = felder[i].name();
			try {
				Field field = felder[i].getClass().getField(name);
				FelderInfo info = field.getAnnotation(FelderInfo.class);
				if (info == null) {
					metaFeldInfos.add(new MetaFeldInfo(felder[i]));
				} else {
					metaFeldInfos.addAll(getMetaFeldInfos(info));
				}
			} catch (NoSuchFieldException nsfe) {
				throw new InternalError("no field " + name + " (" + nsfe + ")");
			}
		}
		return metaFeldInfos;
	}

	private static List<MetaFeldInfo> getMetaFeldInfos(final FelderInfo info) {
		Collection<? extends Enum<?>> enums = getAsList(info);
		List<MetaFeldInfo> metaFeldInfos = new ArrayList<MetaFeldInfo>(enums.size());
		for (Enum<?> enumX : enums) {
			metaFeldInfos.add(new MetaFeldInfo(enumX, info));
		}
		return metaFeldInfos;
	}

	private static Collection<? extends Enum<?>> getAsList(final FelderInfo info) {
		Class<? extends Enum<?>> enumClass = info.type();
		return getAsList(enumClass.getEnumConstants());
	}

	/**
	 * Wandelt das uebergebene Array in eine Liste mit Felder. Seit 0.7.1
	 * duerfen Feld-Enums wie {@link gdv.xport.satz.feld.Feld100} auch
	 * FelderInfo-Annotationen enthalten, die wiederum auf einen Enum verweisen.
	 *
	 * @param felder the felder
	 * @return the feld info list
	 */
	private static List<Enum<?>> getAsList(final Enum<?>[] felder) {
		List<Enum<?>> feldList = new ArrayList<Enum<?>>(felder.length);
		for (int i = 0; i < felder.length; i++) {
			String name = felder[i].name();
			try {
				Field field = felder[i].getClass().getField(name);
				FelderInfo info = field.getAnnotation(FelderInfo.class);
				if (info == null) {
					feldList.add(felder[i]);
				} else {
					feldList.addAll(getAsList(info));
				}
			} catch (NoSuchFieldException nsfe) {
				throw new InternalError("no field " + name + " (" + nsfe + ")");
			}
		}
		return feldList;
	}

	/**
	 * Leitet aus dem uebergebenen Feldelement die Parameter ab, um daraus ein
	 * Feld anzulegen und im jeweiligen Teildatensatz einzuhaengen. Zusaetzlich
	 * wird das Feld "Satznummer" vorbelegt, falls es in den uebergebenen
	 * Feldern vorhanden ist.
	 * <p>
	 * TODO: Vorsatz wird noch nicht richtig behandelt, da die ersten 6 Felder
	 * hier etwas anders behandelt wird.
	 * </p>
	 *
	 * @param feldX das Feld-Element
	 * @param tds der entsprechende Teildatensatz
	 */
	protected static void add(final Enum<?> feldX, final Teildatensatz tds) {
		FeldInfo info = MetaFeldInfo.getFeldInfo(feldX);
		Feld feld = Feld.createFeld(feldX, info);
		if (info.nr() < 8) {      // TODO: diese Abfrage ist eigentlich unnoetig
			LOG.debug("using default settings for " + feld);
		} else {
			tds.add(feld);
			if (isSatznummer(feldX)) {
				feld.setInhalt(info.teildatensatz());
			}
		}
	}

	private static boolean isSatznummer(final Enum<?> feldX) {
		return (feldX.name().length() <= 11) && feldX.name().startsWith("SATZNUMMER");
	}

}
