### Overview

| Package | Type | Summary |
|:------- |:---- |:------- |
| _**data**_ | _BasicDataSource_ | base class for configuring data sources |
| | DirectDataSource | configures a data source from **db.properties** |
| | CloudDataSource  | configures a data source from **db.properties** and environment variables |
| _**storage**_ | _SurrogatedItem_ | defines protocol for an item with surrogate key |
| | Surrogated&lt;ItemType&gt; | base class for an item with surrogate key |
| | _SurrogatedComposite_ | defines protocol for a composite item with surrogate key |
| | _HashedItem_ | defines protocol for an item with hash of its contents |
| | Hashed&lt;ItemType&gt; | base class for an item with hash of its contents |
| | _Search&lt;ItemType&gt;_ | defines  protocol for a repository searchable by hash |

### Discussion

A persistence layer needs its data source configured and injected during process startup.
The provided data source classes use [Spring][spring] to do this for [Hibernate][hibernate].

Most domain model items should be persisted in a backing store with generated [surrogate keys](#surrogate-keys).
Model items that need to be ensured unique should hash their contents and store the hash along with the data.
These ideas are capture in the JPA annotated base classes and types in this library.
The [model diagram](#model-diagram) below shows how this project makes use of these ideas.

### Surrogate Keys

Whether to use a surrogate key or a natural key as the primary key for a table is an essential solution design 
[trade-off][key-trade-offs].
Surrogate keys are great for achieving referential integrity independent of the actual business data.
However, they don't ensure uniqueness of the rows when that's wanted.

### Hashed Items

This solution design uses a [long hash][long-hash] of the item data to ensure that each item row is stored uniquely, and
by including the hash in each row with an associated index of the hashes, it also supports rapid item fetches given
their hash values, which can be regenerated from the original data.
So, the item data itself gets used to look it up.

### Composite Items

Composite items are handled in a special way.
**Surrogated** items know whether they are also composites, as indicated by implementing **SurrogatedComposite**.
When they are, as they get saved, they ensure their parts are saved first, so that the surrogate keys of the parts
are assigned **before** that of the composite itself.

### Model Diagram
![Domain Model][model-diagram]

[model-diagram]: ../images/contacts-models.png
[hibernate]: http://hibernate.org/orm/
[spring]: https://spring.io/projects/spring-framework

[long-hash]: ../educery-utils#discussion
[key-trade-offs]: https://www.mssqltips.com/sqlservertip/5431/surrogate-key-vs-natural-key-differences-and-when-to-use-in-sql-server/
