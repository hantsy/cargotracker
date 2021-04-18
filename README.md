# Eclipse Cargo Tracker - Applied Domain-Driven Design Blueprints for Jakarta EE

![compile and build](https://github.com/hantsy/cargotracker/workflows/build/badge.svg)
![Integration Test with Arquillian Payara Managed Container](https://github.com/hantsy/cargotracker/workflows/it-with-arq-payara-managed/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)

[![CircleCI](https://circleci.com/gh/hantsy/cargotracker.svg?style=svg)](https://circleci.com/gh/hantsy/cargotracker)

![Integration Test with Arquillian WildFly Managed Container](https://github.com/hantsy/cargotracker/workflows/it-with-arq-wildfly-managed/badge.svg)
![Integration Test with Arquillian WildFly Embedded Container](https://github.com/hantsy/cargotracker/workflows/it-with-arq-wildfly-embedded/badge.svg)

> **This is my personal fork of [eclipse-ee4j/cargotracker](https://github.com/eclipse-ee4j/cargotracker), some features planned to sync to the upstream project is in progress.**

> For the detailed introduction to this project, go to the original project website: https://eclipse-ee4j.github.io/cargotracker/.

Follow the following steps to try it on your local system.

## Prerequisites

* Java 11
* Apache Maven 3.8.1
* Git
* Docker
* [Payara 5](https://www.payara.fish/downloads/) or [WildFly](https://www.wildfly.org)

## Building and Running the Application

### Preparing a Running Postgres Database

In this project, we use Postgres as an example.

There is a *docker-compose.yaml* file available in the project root folder.

Switch to the project root folder, and run  the following command to start a Postgres instance in Docker  container.

```bash
docker-compose up postgres
```

### Run Application on Payara 5

Run the following command to run the application on Payara 5 using cargo maven plugin.

```bash
mvn clean package cargo:run
```
Open your browser, go to http://localhost:8080/cargo-tracker

### Run Application on WildFly 

Run the following command to run the application on WildFly  using the official WildFly maven plugin.

```bash
mvn clean package wildfly:run -Pwildfly
```
Open your browser, go to http://localhost:8080/cargo-tracker


## Exploring the testing codes

Cargo Tracker's testing is done using JUnit and [Arquillian](http://arquillian.org/). There are several Maven profiles configured for running the testing codes against various adapters.

### Testing Locally with Payara

For testing locally you will first need to run a Payara 5 server.

You can do that with the following script:

```shell script
wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/5.2020.7/payara-5.2020.7.zip
unzip payara-5.2020.7.zip && cd payara5/bin
./asadmin start-domain
```

Now for running the tests:

```shell script
mvn -Ppayara -DskipTests=false test
```
> I also added configuration of running tests on Payara Embedded and Payara Micro adapters, but they failed. See [the issues](https://github.com/payara/ecosystem-support/issues/created_by/hantsy) I reported on Payara issue tracker.

###  Testing with WildFly Remote Adapter

Getting the latest WildFly distribution from [the official WildFly website](https://www.wildfly.org).

You can do that with the following script:

```shell script
wget https://download.jboss.org/wildfly/23.0.0.Final/wildfly-23.0.0.Final.zip
unzip wildfly-23.0.0.Final.zip && cd wildfly-23.0.0.Final/bin
./standalone -c standalone-full.xml
```

Follow the [WildFly Admin Guide](https://docs.wildfly.org/23/Admin_Guide.html#add-user-utility) to add a new admin user(`admin/admin@123`).

Now for running the tests against WildFly:

```shell script
mvn clean verify -Parq-wildfly-remote -DskipTests=false
```

###  Testing with WildFly Managed Adaper

Run Arquillian tests against WildFly Managed Adaper.

```shell script
mvn clean verify -Parq-wildfly-managed -DskipTests=false
```

###  Testing with WildFly Embedded Adaper

Run the following command to run testing codes on an embedded WildFly.

```shell script
mvn clean verify -Parq-wildfly-embedded -DskipTests=false
```

