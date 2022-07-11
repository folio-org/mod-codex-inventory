# mod-codex-inventory

Copyright (C) 2017-2022 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Prerequisites

* Java 11 JDK
* Maven 3.3.9

## Introduction

Codex Wrapper for Inventory

## Decisions

### No required permissions

No permissions are required to access the endpoints. These would either require the codex multiplexer or downstream clients to know about the specific codex sources, which would introduce a subtle dependency.

Instead this module relies on whether the eventual client has been granted the relevant inventory storage permissions..

## Additional information

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

### Issue tracker

See project [MODCXINV](https://issues.folio.org/browse/MODCXINV)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### ModuleDescriptor

See the built `target/ModuleDescriptor.json` for the interfaces that this module
requires and provides, the permissions, and the additional module metadata.

### API documentation

This module's [API documentation](https://dev.folio.org/reference/api/#mod-codex-inventory).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/dashboard?id=org.folio%3Amod-codex-inventory).

### Download and configuration

The built artifacts for this module are available.
See [configuration](https://dev.folio.org/download/artifacts) for repository access,
and the [Docker image](https://hub.docker.com/r/folioorg/mod-codex-inventory/).


