# SEPIA – SBOM Exchange Procedures, Interfaces and Architecture

* Presenter: Hans-Malte Kern, Robert Bosch GmbH
* Date: 2024-09-12
* Language: EN
* Event: BitKom Open Source Forum 2024 (bfoss24)
* Location: Erfurt, Germany
* Link: https://www.bitkom.org/bfoss24

## Teaser English (on the fly translation)

With OpenChain we have an international standard (ISO/IEC 5230) on the most 
important requirements for an open source license compliance program. 
SPDX and Cyclone DX each define their own format for exchanging information 
on software components. However, both formats are not clear enough and allow 
the same information to be documented in different ways. 

Robert Bosch occupies different positions in the value chain - Tier 0, 
Tier 1 to Tier X - or in other words: original creator of a software, 
integrator, refiner and supplier, marketer. And here we notice that the 
definition of the formats is not sufficient. Even with internal value chains, 
due to the different views of SPDX and CycloneDX, we always have a break in 
content or format and therefore a tool break, which makes complete automation 
almost impossible. 

We see a variety of different requirements from external customers in the 
automotive sector regarding the exchange format and their own solutions, 
some of which are based on existing standards. Additional requirements such 
as the EU Cyber Resilience Act (CRA) or BSI TR-3183 must be taken into 
account in an SBOM. 

At Bosch, we have started to define a set of meta information that must be 
exchanged when software components are shared internally. This meta 
information can be displayed in both SPDX and CycloneDX and the data types 
of the individual elements are automatically checked using a schema. This 
allows us to automatically determine whether we can further process an SBOM 
automatically. We are currently planning to make this schema available to our 
suppliers in order to increase the quality of the SBOMs supplied. 

In order to be able to use SBOMs more effectively and, above all, more 
efficiently along the value chain, we need more such schemes, e.g. one for 
the automotive sector. And I would like to use the presentation for this:
to define an initiative for the standardization of SBOM and the development 
of uniform validation schemes for individual industrial sectors. I would 
like to start in the automotive sector, as the automotive industry is a key 
industry for Germany and the multitude of formats with mandatory fields from 
individual automotive manufacturers has progressed so far that reusing 
meta-information is extremely difficult.

