/*
 * Copyright (c) 2017 by Oliver Boehm
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
 * (c)reated 13.02.17 by oliver (ob@oasd.de)
 */
package gdv.xport.srv.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Der PingController dient als Endpunkt fuer den Ping-Service. Dieser dient
 * hauptsaechlich zur Fehlersuche.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 */
@RestController
public final class PingController {

    /**
     * Ein sehr einfacher PING-Service, der immer nur "OK" zurueckgibt.
     *
     * @return "OK"
     */
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

}