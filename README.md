# Eclipse Cargo Tracker - Applied Domain-Driven Design Blueprints for Jakarta EE

[![build](https://github.com/hantsy/cargotracker/actions/workflows/build.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/build.yml)

[![arq-glassfish-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-managed.yml)
[![arq-glassfish-remote](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-remote.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-glassfish-remote.yml)

[![arq-wildfly-managed](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-managed.yml)
[![arq-wildfly-remote](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-remote.yml/badge.svg)](https://github.com/hantsy/cargotracker/actions/workflows/arq-wildfly-remote.yml)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)

> **This is a personal fork of [Eclipse EE4J CargoTracker](https://github.com/eclipse-ee4j/cargotracker), I'm also [a contributor of the CargoTracker project](https://github.com/eclipse-ee4j/cargotracker/graphs/contributors).**

> For the detailed introduction to the CargoTracker project, go to the upstream project website: https://eclipse-ee4j.github.io/cargotracker/.

There are some highlights when comparing to the upstream project.

* Utilize Docker to run a Postgres Database in both development and production to erase the risk brought by different environments.
* Add support to run application on WildFly.
* Add several fine-grained Maven profiles for varied Arquillian Container adapters, which is derived from [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate).
* Replace Primefaces with simple Bootstrap css style and plain Facelets templates, clean up and reorganize all Facelets templates.
* Add a plenty of testing codes to cover more use cases.
* Add GitHub Actions workflows to build the project and run testing codes, and generate code quality report via Jacoco, SonarCloud.

I have also ported the original [Cargotracker regapp](https://github.com/citerus/dddsample-regapp) which was written in Spring and Swing UI to Jakarta EE/CDI world, check the following projects.
* [cargotracker-regapp](https://github.com/hantsy/cargotracker-regapp): CDI/Weld + JavaFX
* [quarkus-cargotracker-regapp](https://github.com/hantsy/quarkus-cargotracker-regapp): Quarkus + Quarkus FX Extension/JavaFX

## Build and Run 

### Prerequisites

* Java 21
* Apache Maven 3.9.0
* Git
* Docker
* [GlassFish v7](https://github.com/eclipse-ee4j/glassfish) or [WildFly 30+](https://www.wildfly.org)

### Startup PostgresSQL Database

There is a *docker-compose.yaml* file available in the project root folder.

In your terminal, switch to the project root folder, and run the following command to start a Postgres instance in Docker container.

```bash
docker compose up postgres
```

### GlassFish

Run the following command to run the application on GlassFish v7 using cargo maven plugin.

```bash
mvn clean package cargo:run -Pglassifsh
```
### WildFly 

Run the following command to run the application on WildFly using the official WildFly maven plugin.

```bash
mvn clean wildfly:run -Pwildfly
```
When the application is deployed sucessfully, open your browser, go to http://localhost:8080/cargo-tracker

## Testing

Cargo Tracker's testing is done using [JUnit](https://junit.org) and [Arquillian](http://arquillian.org/). 

There are several Maven profiles configured for running the testing codes against varied Arquillian Container adapters.

> Note: Before running the Arquillian integration tests, make sure there is a running Postgres database ready for test, check the Build section for more details.

###  GlassFish

Open a terminal window, execute the following command to run Arquillian tests against Payara Managed Adapter.

```bash
mvn clean verify -Parq-glassfish-managed
```

Or run this command instead to run tests against a Glassfish Remote adapter.

> Note: Make sure there is a running Glassfish server on your local machine.

```bash 
mvn clean verify -Parq-glassfish-remote 
```

###  WildFly

Run the following command to run Arquillian tests against WildFly Managed Adapter.

```bash
mvn clean verify -Parq-wildfly-managed

// or run on a remote WildFly server
mvn clean verify -Parq-wildfly-remote 
```

> More details about the Arquillian adapter's configuration, go to [Jakarta EE 9 template project](https://github.com/hantsy/jakartaee9-starter-boilerplate) or [Jakarta EE 10 template project](https://github.com/hantsy/jakartaee10-starter-boilerplate), and follow [this comprehensive guide](https://hantsy.github.io/jakartaee9-starter-boilerplate/) to research them yourself.
