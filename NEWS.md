## IN-PROGRESS

* Requires `instance-storage` `9.0` ([MODCXINV-70](https://issues.folio.org/browse/MODCXINV-70))

## 2.3.0 2022-06-15

* Upgrade to RMB to 34.0.0 ([MODCXINV-68](https://issues.folio.org/browse/MODCXINV-68))

## 2.2.0 2022-02-22

* Upgrade to RMB to 33.2.4 (CVE-2021-44228) ([MODCXINV-61](https://issues.folio.org/browse/MODCXINV-61), [MODCXINV-66](https://issues.folio.org/browse/MODCXINV-66))

## 2.1.0 2021-10-05

* Requires `instance-storage 7.0 or 8.0`

## 2.0.0 2021-06-11

* `embed_postgres` command line option is no longer supported ([MODCXINV-56](https://issues.folio.org/browse/MODCXINV-56))
* Upgrades to RAML-Module-Builder 33.0.0 ([MODCXINV-56](https://issues.folio.org/browse/MODCXINV-56))
* Upgrades to Vert.x 4.1.0.CR1 ([MODCXINV-56](https://issues.folio.org/browse/MODCXINV-56))

## 1.9.0 2021-03-15

* Migrate to vert.x 4.0.0 ([MODCXINV-54](https://issues.folio.org/browse/MODCXINV-54))
* Upgraded to RAML Module Builder 32.2.0 ([MODCXINV-54](https://issues.folio.org/browse/MODCXINV-54))
* Upgraded to OKAPI 4.3.0 ([MODCXINV-54](https://issues.folio.org/browse/MODCXINV-54))

## 1.8.0 2020-10-07

* Requires JDK 11 ([MODCXINV-49](https://issues.folio.org/browse/MODCXINV-49))
* Upgraded to RAML Module Builder 31.1.0 ([MODCXINV-48](https://issues.folio.org/browse/MODCXINV-48))

## 1.7.0 2020-06-12

* Upgrades to RAML Module Builder 30.0.0 ([MODCXINV-46](https://issues.folio.org/browse/MODCXINV-46))

## 1.6.0 2020-03-11

* Upgrades to RAML Module Builder 29.3.0 ([MODCXINV-44](https://issues.folio.org/browse/MODCXINV-44))

## 1.5.0 2019-07-24

 * Requires `instance-storage` interface 7.0 ([MODCXINV-40](https://issues.folio.org/browse/MODCXINV-40))
 * Adds initial module metadata ([FOLIO-2003](https://issues.folio.org/browse/FOLIO-2003))
 * Improves documentation ([FOLIO-473](https://issues.folio.org/browse/FOLIO-473))

## 1.4.0 2018-12-01
 * [MODCXINV-38](https://issues.folio.org/browse/MODCXINV-38) Adapt to new structure of alternativeTitles from Inventory
 * [MODCXINV-37](https://issues.folio.org/browse/MODCXINV-37) Use description fields in RAML JSON schema

## 1.3.0 2018-10-11

 * [MODCXINV-36](https://issues.folio.org/browse/MODCXINV-36) Use locations 3.0 (or later)
 * [MODCXINV-35](https://issues.folio.org/browse/MODCXINV-35) Map repeatable instance formatId (was: simple text field)
 * [MODCXINV-34](https://issues.folio.org/browse/MODCXINV-34) Remove deprecated property `instance.urls` from test sample
 * [MODCXINV-33](https://issues.folio.org/browse/MODCXINV-33) Fix bad pageable
 * [MODCXINV-32](https://issues.folio.org/browse/MODCXINV-32) Adapt to Instance.edition being replaced with
   Instance.editions in `instance-storage`

## 1.2.0 2018-09-0

 * [MODCXINV-31](https://issues.folio.org/browse/MODCXINV-31) Update to RAML 1.0
 * [MODCXINV-28](https://issues.folio.org/browse/MODCXINV-28) Update resource type map with RDA content terms
 * [MODCXINV-28](https://issues.folio.org/browse/MODCXINV-28) Update dependencies, instance-types (2.0), instance-formats (2.0)

## 1.1.0 2018-04-10

 * [MODCXINV-19](https://issues.folio.org/browse/MODCXINV-19) New new locations for filtering
 * [MODCXINV-20](https://issues.folio.org/browse/MODCXINV-20) codex-instance/id endpoint should return 401 instead of 500
 * [MODCXINV-21](https://issues.folio.org/browse/MODCXINV-21) Fix code smells as reported by SQ
 * [MODCXINV-22](https://issues.folio.org/browse/MODCXINV-22) /codex-instance?limit=1 should return 400 instead of 500
 * [MODCXINV-23](https://issues.folio.org/browse/MODCXINV-23) /codex-instance/lang=123 should return 400 instead of 200
 * [MODCXINV-24](https://issues.folio.org/browse/MODCXINV-24) SQ fixes
 * [MODCXINV-25](https://issues.folio.org/browse/MODCXINV-25) Fix invalid UUIDs

## 1.0.3 2018-03-13

 * [MODCXINV-16](https://issues.folio.org/browse/MODCXINV-16) Improve test coverage
 * [MODCXINV-17](https://issues.folio.org/browse/MODCXINV-17) Use CQLUtil from okapi-common
 * [MODCXINV-18](https://issues.folio.org/browse/MODCXINV-18) Update to RMB 19.0.0

## 1.0.2 2018-02-02

 * [MODCXINV-13](https://issues.folio.org/browse/MODCXINV-13) Ignore ext.selected

## 1.0.1 2018-01-24

 * [MODCXINV-6](https://issues.folio.org/browse/MODCXINV-6) Report module on start
 * [MODCXINV-9](https://issues.folio.org/browse/MODCXINV-9) Support query: (title="a*") and source=("kb" or "local")
 * [MODCXINV-11](https://issues.folio.org/browse/MODCXINV-11) Fix search on location and subject

## 1.0.0 2018-01-09

 * [MODCXINV-8](https://issues.folio.org/browse/MODCXINV-8) implement resultInfo schema

## 0.0.3 2018-01-04

 * [MODCXINV-7](https://issues.folio.org/browse/MODCXINV-7) Convert from Codex CQL to Inventory CQL
 * [MODCXINV-5](https://issues.folio.org/browse/MODCXINV-5) Update for instance-storage version 4

## 0.0.2 2017-12-19

 * Avoid passing non-UUID to inventory-storage MOXCXINV-4

## 0.0.1 2017-12-19

 * First release. Relies on instance-storage interface 3.0 and
   various types interfaces e.g. contributor-types 1.0.
 * This version has full record conversion.
 * No CQL queries are checked or re-written.
