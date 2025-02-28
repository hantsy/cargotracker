# Eclipse Cargo Tracker - Applied Domain-Driven Design Blueprints for Jakarta EE

[![build](https://github.com/hantsy/cargotracker/actions/workflows/build.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/build.yml)

[![arq-glassfish-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml)
[![arq-glassfish-remote](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-remote.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-remote.yml)

[![arq-wildfly-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml)
[![arq-wildfly-remote](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-remote.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-remote.yml)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)

> [!WARNING] This is my personal fork of [Eclipse EE4J CargoTracker](https://github.com/eclipse-ee4j/cargotracker). I am also [a contributor to the CargoTracker project](https://github.com/eclipse-ee4j/cargotracker/graphs/contributors).

> [!NOTE] For a detailed introduction to the CargoTracker project, visit the upstream project website: https://eclipse-ee4j.github.io/cargotracker/.

Here are some highlights compared to the upstream project:

* Utilizes Docker to run a PostgreSQL Database in both development and production environments, eliminating risks associated with different environments.
* Adds support for running the application on WildFly.
* Includes several fine-grained Maven profiles for various Arquillian Container adapters, derived from the [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate).
* Replaces PrimeFaces with simple Bootstrap CSS styles and plain Facelets templates, cleaning up and reorganizing all Facelets templates.
* Adds extensive testing code to cover more use cases.
* Integrates GitHub Actions workflows to build the project, run tests, and generate code quality reports via Jacoco and SonarCloud.

I have also ported the original [Cargotracker regapp](https://github.com/citerus/dddsample-regapp), originally written in Spring and Swing UI, to the Jakarta EE/CDI world. Check out the following projects:
* [cargotracker-regapp](https://github.com/hantsy/cargotracker-regapp): CDI/Weld + JavaFX
* [quarkus-cargotracker-regapp](https://github.com/hantsy/quarkus-cargotracker-regapp): Quarkus + Quarkus FX Extension/JavaFX

## Build and Run 

### Prerequisites

* Java 21
* Apache Maven 3.9.0 +
* Git
* Docker
* [GlassFish v7](https://github.com/eclipse-ee4j/glassfish) or [WildFly 30+](https://www.wildfly.org)

### Start PostgreSQL Database

A *docker-compose.yaml* file is available in the project root folder.

In your terminal, switch to the project root folder and run the following command to start a PostgreSQL instance in a Docker container:

```bash
docker compose up postgres
```

### GlassFish

Run the following command to run the application on GlassFish v7 using the Cargo Maven plugin:

```bash
mvn clean package cargo:run -Pglassfish
```

### WildFly 

Run the following command to run the application on WildFly using the official WildFly Maven plugin:

```bash
mvn clean wildfly:run -Pwildfly
```

When the application is successfully deployed, open your browser and go to http://localhost:8080/cargo-tracker

## Testing

Cargo Tracker's testing is done using [JUnit](https://junit.org) and [Arquillian](http://arquillian.org/).

There are several Maven profiles configured for running the tests against various Arquillian Container adapters.

> [!Note]
> Before running the Arquillian integration tests, ensure there is a running PostgreSQL database ready for testing. Check the Build section for more details.

### GlassFish

Open a terminal window and execute the following command to run Arquillian tests against the GlassFish Managed Adapter:

```bash
mvn clean verify -Parq-glassfish-managed
```

Or run this command to run tests against a GlassFish Remote adapter:

> Note: Ensure there is a running GlassFish server on your local machine.

```bash 
mvn clean verify -Parq-glassfish-remote 
```

### WildFly

Run the following command to run Arquillian tests against the WildFly Managed Adapter:

```bash
mvn clean verify -Parq-wildfly-managed

// or run on a remote WildFly server
mvn clean verify -Parq-wildfly-remote 
```

> [!NOTE] 
> For more details about the Arquillian adapter's configuration, visit the [Jakarta EE 9 template project](https://github.com/hantsy/jakartaee9-starter-boilerplate) or the [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate), and follow [this comprehensive guide](https://hantsy.github.io/jakartaee9-starter-boilerplate/) to learn more.
