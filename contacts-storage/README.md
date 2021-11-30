### Overview

| Package | Type | Summary |
|:------- |:---- |:------- |
| _**storage**_ | ContactStorage | stores and fetches contacts |
| | AddressStorage | stores and fetches mailing addresses |
| | PhoneStorage | stores and fetches phone numbers |
| | EmailStorage | stores and fetches email addresses |
| | PersistenceContext | configures the JPA persistence mechanisms above |

### Discussion

The _**storage**_ package in this library contains types that define how to store, fetch, and search for 
the model items in the backing store.
When configured by the **PersistenceContext**, the storage types get converted into full blown **CrudRepository** 
implementations by the [Spring Data JPA][spring-jpa] framework.

[model-diagram]: ../images/contacts-models.png
[composite]: ../educery-storage#composite-items
[spring-jpa]: https://spring.io/projects/spring-data-jpa
[valid]: https://en.wikipedia.org/wiki/Bean_Validation
