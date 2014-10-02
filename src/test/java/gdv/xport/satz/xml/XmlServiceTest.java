/*
 * Copyright (c) 2014 by Oli B.
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
 * (c)reated 15.08.2014 by Oli B. (ob@aosd.de)
 */

package gdv.xport.satz.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gdv.xport.feld.Bezeichner;
import gdv.xport.feld.Feld;

import org.junit.Test;

/**
 * Unit-Test fuer {@link XmlService}-Klasse
 *
 * @author oliver
 * @since 1.0 (15.08.2014)
 */
public class XmlServiceTest extends AbstractXmlTest {

    private static XmlService xmlService = XmlService.getInstance();

    /**
     * Einfache Test-Methode fuer {@link XmlService#getSatzart(int)}.
     */
    @Test
    public void testGetSatzart() {
        getSatzart(100);
    }

    /**
     * Hier begutachten wir etwas genauer den von
     * {@link XmlService#getSatzart(int)} zurueckgelieferten Satz.
     */
    @Test
    public void testGetSatzart200() {
        SatzXml satz200 = getSatzart(200);
        assertEquals(2, satz200.getTeildatensaetze().size());
        Feld inkasso = satz200.getFeld(Bezeichner.INKASSOART);
        assertEquals(1, inkasso.getAnzahlBytes());
        assertEquals(43, inkasso.getByteAdresse());
    }

    private static SatzXml getSatzart(final int satzart) {
        SatzXml satz = xmlService.getSatzart(satzart);
        assertNotNull(satz);
        assertEquals(satzart, satz.getSatzart());
        return satz;
    }

}
