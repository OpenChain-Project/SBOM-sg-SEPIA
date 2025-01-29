# SEPIA – SBOM Exchange Procedures, Interfaces and Architecture

* Presenter: Hans-Malte Kern, Robert Bosch GmbH
* Date: 2024-09-12
* Language: EN
* Event: BitKom Open Source Forum 2024 (bfoss24)
* Location: Erfurt, Germany
* Link: https://www.bitkom.org/bfoss24


## Teaser Deutsch (Published in BitKom Open Source Forum Program 2024)

Mit OpenChain haben wir einen internationalen Standard (ISO/IEC 5230) zu den wichtigsten Anforderungen an
ein Open-Source-Lizenz-Compliance-Programm. SPDX und Cyclone DX definieren jeweils ein eigenes Format um
Informationen zu Softwarekomponenten austauschen zu können. Beide Formate sind jedoch nicht eindeutig
genug und erlauben die Dokumentation derselbe Information auf unterschiedliche Weisen.
In der Wertschöpfungskette nimmt die Robert Bosch unterschiedliche Positionen ein – Tier-0, Tier-1 bis Tier-X –
oder mit anderen Worten: Ur-Ersteller einer Software, Integrator, Veredler und Lieferant, In-Markt-Bringer. Und
hier merken wir, dass die Festlegung der Formate nicht ausreichend sind. Selbst bei internen Wertschöpfungsket-
ten haben wir Aufgrund der unterschiedlichen Sichten von SPDX und CycloneDX immer wieder einen Inhalts- bzw.
Format- und daher einen Toolbruch, was eine vollständige Automatisierung nahezu unmöglich macht.
Bei externen Kunden im Automobilbereich sehen wir eine Vielzahl von unterschiedlichen Anforderungen bzgl. des
Austauschformats und eigenen Lösungen z.T. basierend auf den existierenden Standards. Zusätzliche Anforderun-
gen wie z. B. des EU-Gesetz über Cyberresilienz (CRA) oder BSI TR-3183 müssen bei einer SBOM berücksichtigt
werden.

Bei Bosch haben wir angefangen einen Satz an Meta-Informationen zu definieren, die bei der internen Weiterga-
be von Softwarekomponenten ausgetauscht werden müssen. Diese Meta-Informationen lassen sich sowohl in
SPDX als auch CycloneDX darstellen und die Datentypen der einzelnen Elemente werden automatisch über ein
Schema geprüft. Somit können wir automatisch feststellen ob eine SBOM von uns automatisiert weiterbearbeitet
werden kann. Momentan planen wir diese Schema auch unseren Lieferanten zur Verfügung zu stellen, um die
Qualität der zugelieferten SBOMs zu erhöhen.

Um SBOMs effektiver und vor allem effizienter entlang der Wertschöpfungskette nutzen zu können brauchen wir
mehr solcher Schemes, z. B. eins für den Automobilbereich. Und hierfür würde ich gerne die Präsentation nutzen:
eine Initiative für die Standardisierung von SBOM und die Entwicklung von einheitlichen Validierungs-Schemas für
einzelne Industriebereiche zu definieren. Anfangen würde ich gerne im Automobilbereich, da die Automobilindus-
trie für Deutschland eine Schlüsselindustrie ist und die Vielzahl von Formaten mit Pflichtfeldern der einzelnen
Automobilhersteller so weit vorangeschritten ist, dass eine Wiederverwendung von Meta-Informationen extrem
schwer fällt.

