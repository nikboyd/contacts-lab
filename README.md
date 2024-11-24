# contacts-lab

A [lab](#lab-notes) project for managing contact information.

### Overview

| Section | Notes |
|:------- |:----- |
| [contacts-manager](contacts-manager#overview) | a contact manager application |
| [contacts-boot](contacts-boot#overview) | a Spring Boot app for launching the web service |
| [contacts-service](contacts-service#overview) | a web service for managing contacts |
| [contacts-storage](contacts-storage#overview) | contact storage classes and types |
| [contacts-domain](contacts-domain#overview) | contact domain model classes and the service interface |
| [educery-storage](educery-storage#overview) | general storage classes and types |
| [educery-utils](educery-utils#overview) | general utility classes and functions |
| [building](#building-this-project) | how to build this project |
| [lab notes](#lab-notes) | notes about project intent and limitations |
| [versions](#library-versions) | notes about the various libraries used |
| [lab tests](#lab-tests) | notes about testing approaches taken with this lab project |

### Building This Project

This [lab](#lab-notes) project has been tested and works with both [OpenJDK][open-jdk] 8 and 11, and with 
the [GraalVM][graal-vm] versions of those. So, make sure you have one of those available.
That said, [contacts-manager](contacts-manager#overview) app _requires_ JDK 11.
So, use that by preference if you build the full stack.
Also, make sure you have a recent version of [Maven][maven] installed.

To build this project, clone this repository. Then, run the following command in the cloned project folder:

```
mvn -U -B clean install
```

This will build the project libraries and run their tests, including storage and web service tests.
Test results will appear in the console.

### Project Operation

After you build the project, you can [operate the web service locally](contacts-service#contact-service-api) 
in order to explore and test out the web service through its API.
While running the web service in a separate command window, you can launch the 
[contact manager](contacts-manager#operating-locally) from a different command window.
Alternately, you can launch the [contact manager](contacts-manager#operating-locally) alone.
It will check whether the web service is running locally. If not, it will launch it.
You can then [use the contact manager](contacts-manager#gallery) which in turn uses the running web service.

### Lab Notes

This lab project focuses on solving a set of technical challenges in the context of a limited problem domain:
**contact management**. 
That said, the solution design only solves some of the problems associated with contacts:

* contacts can only have associated phone numbers, email addresses, and mailing addresses
* only the following types of those are supported: HOME, WORK, MOBILE, BILLING, SHIPPING
* phone numbers and mailing addresses are limited to USA formats

Some of these limitations are in place that support and illustrate specific solution features:

* simpler data models
* data and format validations
* duplicate entries in the backing store are eliminated by design
* data is validated, normalized, and its contents hashed when saved
* hashes can then be used for fast indexed searches by the persistence layer
* saving parts of composite items are handled bottom up, esp. maps and sets

In spite of these limitations, this project shows some good design choices and patterns applicable to web services:

* web service self-hosting with [Spring Boot][spring-boot] and embedded [Tomcat][tomcat]
* REST-like [JAX-RS][jax-rs] web service APIs built with [Apache CXF][apache-cxf]
* web service API test clients built with [JUnit][junit] and [Apache CXF][apache-cxf] proxies
* included API documentation built with [Enunciate][enunciate]
* API payloads limited to [JSON][bind-json], although XML could also be added
* object-relational mappings ORM with [JPA][persist] and [Hibernate ORM][hibernate]
* persistence layers built with [Spring Data JPA][spring-jpa]
* backing storage for objects uses [H2][h2-db] for tests and 
* anticipates [PostgreSQL][pg-db] for general storage cases
* JPA annotated model classes generate their backing database tables

### Library Versions

The following table shows the library versions have been used in this solution.
As can be seen below, there are many libraries that must be made to work well together.
This was one of the technical challenges to be solved, as is generally true of even simple projects of this kind.
Where possible, more recent versions have been used and compatibility issues resolved.

| Library | Maven | Version | Notes |
|:------- |:----- |:------- |:----- |
| [JavaFX][java-fx] | javafx-fxml | 13.0.2 | application framework |
| [JUnit][junit] | junit:junit | 4.13.1 | testing framework |
| [Enunciate][enunciate]     | enunciate-swagger | 2.14.0 | API documentation generator |
| [Apache CXF][apache-cxf]   | cxf-rt-rs-client | 3.5.5 | JAX-RS web service framework |
| [Spring][spring] | spring-context | 5.3.39 | dependency injection |
| [Spring Boot][spring-boot] | spring-boot-starter-web | 2.6.15 | web service self-hosting |
| [Spring Data JPA][spring-jpa] | spring-data-jpa | 2.5.10 | JPA repository framework |
| [Hibernate ORM][hibernate] | hibernate-entitymanager | 5.6.5 | object-relational mapping |
| [H2][h2-db] | com.h2.database:h2 | 2.1.210 | test object storage |
| [PostgreSQL][pg-db] | postgresql | 42.2.25 | general object storage |
| [Validation][valid] | hibernate-validator | 5.4.3 | data validation framework |
| [JPA][persist] | javax.persistence-api | 2.2 | annotated persistence framework |
| [Mail][mail] | javax.mail:mail | 1.4.7 | email library |
| [Time][std-time] | java.time.* | 8+ | standard time lib, after [Joda-time][joda-time] |
| [Binding JSON][bind-json] | jackson-jaxrs-json-provider | 2.9.8 | JSON data binding |
| [Binding XML][bind-xml] | com.sun.xml.bind:jaxb-impl | 2.3.0 | XML data binding |
| [Binding API][bind-xml] | jakarta.xml.bind-api | 2.3.0 | data binding framework |
| [Logging Facade][slf4j] | slf4j-log4j12 | 1.7.36 | logging abstraction |
| [Logging Implement][log4j] | log4j-core | 2.20.0 | logging framework |
| [Cryptography][crypto] | bcprov-jdk18on | 1.78.1 | cryptography algorithms |

### Lab Tests

| Library | Test Class | Summary |
|:------- |:---------- |:------- |
| contacts-service | ServiceTest | tests the main usage scenarios of the web service endpoints |
| contacts-domain | RepositoryTest | tests the storage and retrieval of the main domain classes |
|  | ContentTest | tests the main domain classes and their serialization to and from JSON |

This lab project focuses primarily on web service acceptance tests and backing storage tests.
Comprehensive unit tests are expensive and prone to change over time as library parts evolve.
So, while this lab uses [JUnit][junit] as its test framework, it limits how that gets used.

The test classes in **contacts-service** confirm proper operation of the web service.
[ServiceTest][service-test] ensures that the [ContactFacade][contact-facade] implements the web service API properly, 
esp. those operations that will be combined in their typical sequences.
To simplify its tests, [ServiceTest][service-test] delegates some responsibilities to 
[ClientProxy][client-proxy] and [TestBase][test-base].

[ClientProxy][client-proxy] wraps the web service interface [IContactService][contact-api].
It also converts the domain model objects to JSON before each endpoint call, and then converts the web service 
responses from JSON back to one of the model objects after checking the response status code.

The tests found in **contacts-domain** focus primarily on behaviors needed by the service layer.
[ContentTest][content-test] ensures that the domain model objects can be converted to and from JSON, as the web 
service needs that ability for proper operation of the RESTish web service API and the [ContactFacade][contact-facade].

[RepositoryTest][storage-test] ensures that the storage layer works properly for the service layer, esp. those operations
that store and retrieve the domain model objects to and from the backing store.

### Software BOM

This project uses a few SBOM packages to manage its library dependencies, including those for:
Spring, Spring Boot, Apache CXF, Log4j.
This approach helps coordinate specific library versions and their upgrades over time.
It also comes in handy when addressing security threats raised by associated tooling such as GitHub's depend-a-bot.

[apache-cxf]: https://cxf.apache.org/
[enunciate]: http://enunciate.webcohesion.com/
[junit]: https://junit.org/junit4/
[std-time]: https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html
[joda-time]: https://www.joda.org/joda-time/
[bind-json]: https://github.com/FasterXML/jackson#providers-for-jax-rs
[hibernate]: http://hibernate.org/orm/
[spring]: https://spring.io/projects/spring-framework
[spring-jpa]: https://spring.io/projects/spring-data-jpa
[spring-boot]: https://spring.io/projects/spring-boot

[java-fx]: https://en.wikipedia.org/wiki/JavaFX
[maven]: https://en.wikipedia.org/wiki/Apache_Maven
[tomcat]: https://en.wikipedia.org/wiki/Apache_Tomcat
[jax-rs]: https://en.wikipedia.org/wiki/Jakarta_RESTful_Web_Services
[persist]: https://en.wikipedia.org/wiki/Jakarta_Persistence
[open-jdk]: https://en.wikipedia.org/wiki/OpenJDK
[graal-vm]: https://en.wikipedia.org/wiki/GraalVM
[mail]: https://en.wikipedia.org/wiki/Jakarta_Mail
[valid]: https://en.wikipedia.org/wiki/Bean_Validation
[bind-xml]: https://en.wikipedia.org/wiki/Jakarta_XML_Binding
[h2-db]: https://en.wikipedia.org/wiki/H2_(DBMS)
[pg-db]: https://en.wikipedia.org/wiki/PostgreSQL
[slf4j]: https://en.wikipedia.org/wiki/SLF4J
[log4j]: https://en.wikipedia.org/wiki/Log4j
[crypto]: https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)

[content-test]: contacts-domain/src/test/java/dev/educery/domain/ContentTest.java#L12
[storage-test]: contacts-domain/src/test/java/dev/educery/domain/RepositoryTest.java#L19
[service-test]: contacts-service/src/test/java/dev/educery/services/ServiceTest.java#L11
[client-proxy]: contacts-service/src/test/java/dev/educery/services/ClientProxy.java#L15
[test-base]: contacts-service/src/test/java/dev/educery/services/TestBase.java#L11
[contact-api]: contacts-service/src/main/java/dev/educery/facets/IContactService.java#L12
[contact-facade]: contacts-service/src/main/java/dev/educery/services/ContactFacade.java#L17
