# Eclipse Cargo Tracker – Applied Domain‑Driven Design Blueprints for Jakarta EE

[![build](https://github.com/hantsy/cargotracker/actions/workflows/build.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/build.yml) [![arq-glassfish-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml) [![arq-wildfly-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)

Eclipse Cargo Tracker is based on the sample application from Eric Evans’ Domain-Driven Design book and demonstrates how to apply DDD principles in a Jakarta EE context.

> [!WARNING]
> This is my personal fork of [Eclipse EE4J CargoTracker](https://github.com/eclipse-ee4j/cargotracker). I am also [a contributor to the CargoTracker project](https://github.com/eclipse-ee4j/cargotracker/graphs/contributors).

> [!NOTE]
> For a detailed introduction to the CargoTracker project, see the upstream website: <https://eclipse-ee4j.github.io/cargotracker/>.

This fork adds a number of practical improvements while keeping the original domain‑driven design examples intact.

### What’s different in this fork

* **Consistent PostgreSQL environment** – Docker configuration lets you run the database identically in development and production.
* **WildFly support** – in addition to GlassFish the app can now start on WildFly via the official Maven plugin.
* **More flexible testing** – fine‑grained Maven profiles based on the [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate) make it easy to switch between Arquillian adapters.
* **Cleaner UI** – PrimeFaces is replaced with lightweight Bootstrap CSS and plain Facelets templates; all views were reorganised and tidied.
* **Expanded test coverage** – additional unit and integration tests exercise more use cases.
* **GitHub Actions automation** – builds, tests and code‑quality reports (Jacoco/SonarCloud) run on every push.

Eric’s book also featured a companion client application, the [ddd sample regapp](https://github.com/citerus/dddsample-regapp), originally built with Spring and Swing.  I’ve since ported that sample to the Jakarta EE ecosystem using the following stacks:

* [cargotracker-regapp](https://github.com/hantsy/cargotracker-regapp) – CDI/Weld + JavaFX
* [quarkus-cargotracker-regapp](https://github.com/hantsy/quarkus-cargotracker-regapp) – Quarkus + Quarkus‑FX extension/JavaFX

---

## Getting started

### Requirements

* Java 21
* Apache Maven 3.9+
* Git
* Docker (for PostgreSQL)
* Application server:
  * [GlassFish v8](https://github.com/eclipse-ee4j/glassfish) **or**
  * [WildFly](https://www.wildfly.org/) (wait for a Jakarta EE 11‑compatible release)

### Starting the database

A `docker-compose.yaml` file sits at the project root. To launch PostgreSQL:

```bash
cd /path/to/cargotracker
docker compose up postgres
```

You only need this step once per terminal session; the tests and the running application both rely on the same container.

### Running the application

#### GlassFish

```bash
mvn clean package cargo:run -Pglassfish
```

#### WildFly

```bash
mvn clean wildfly:run -Pwildfly
```

Once deployment finishes, open your browser at <http://localhost:8080/cargo-tracker>.

## Testing

Tests are written with [JUnit](https://junit.org) and executed by [Arquillian](http://arquillian.org/). Multiple Maven profiles allow you to choose the target container.

> [!Note]
> Make sure the PostgreSQL container is running before launching any integration tests (see “Starting the database” above).

### Execute tests against GlassFish

```bash
mvn clean verify -Parq-glassfish-managed
```

### Execute tests against WildFly

```bash
mvn clean verify -Parq-wildfly-managed
```

> [!NOTE]
> For detailed configuration examples refer to the [Jakarta EE 9 template project](https://github.com/hantsy/jakartaee9-starter-boilerplate) or the [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate) and the accompanying [guide](https://hantsy.github.io/jakartaee9-starter-boilerplate/).
