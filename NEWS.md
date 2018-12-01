## 1.4.0 2018-12-01
 * MODCXINV-38 Adapt to new structure of alternativeTitles from Inventory
 * MODCXINV-37 Use description fields in RAML JSON schema

## 1.3.0 2018-10-11

 * MODCXINV-36 Use locations 3.0 (or later)
 * MODCXINV-35 Map repeatable instance formatId (was: simple text field)
 * MODCXINV-34 Remove deprecated property `instance.urls` from test sample
 * MODCXINV-33 Fix bad pageable
 * MODCXINV-32 Adapt to Instance.edition being replaced with
   Instance.editions in `instance-storage`

## 1.2.0 2018-09-0

 * MODCXINV-31 Update to RAML 1.0
 * MODCXINV-28 Update resource type map with RDA content terms
 * MODCXINV-28 Update dependencies, instance-types (2.0), instance-formats (2.0)

## 1.1.0 2018-04-10

 * MODCXINV-19 New new locations for filtering
 * MODCXINV-20 codex-instance/id endpoint should return 401 instead of 500
 * MODCXINV-21 Fix code smells as reported by SQ
 * MODCXINV-22 /codex-instance?limit=1 should return 400 instead of 500
 * MODCXINV-23 /codex-instance/lang=123 should return 400 instead of 200
 * MODCXINV-24 SQ fixes
 * MODCXINV-25 Fix invalid UUIDs

## 1.0.3 2018-03-13

 * MODCXINV-16 Improve test coverage
 * MODCXINV-17 Use CQLUtil from okapi-common
 * MODCXINV-18 Update to RMB 19.0.0

## 1.0.2 2018-02-02

 * MODCXINV-13 Ignore ext.selected

## 1.0.1 2018-01-24

 * MODCXINV-6 Report module on start
 * MODCXINV-9 Support query: (title="a*") and source=("kb" or "local")
 * MODCXINV-11 Fix search on location and subject

## 1.0.0 2018-01-09

 * MODCXINV-8 implement resultInfo schema

## 0.0.3 2018-01-04

 * MODCXINV-7 Convert from Codex CQL to Inventory CQL
 * MODCXINV-5 Update for instance-storage version 4

## 0.0.2 2017-12-19

 * Avoid passing non-UUID to inventory-storage MOXCXINV-4

## 0.0.1 2017-12-19

 * First release. Relies on instance-storage interface 3.0 and
   various types interfaces e.g. contributor-types 1.0.
 * This version has full record conversion.
 * No CQL queries are checked or re-written.

