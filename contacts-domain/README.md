### Overview

| Package | Type | Summary |
|:------- |:---- |:------- |
| _**domain**_  | Contact | a named [composite][composite] of phones, emails, addresses |
| | MailAddress | a USA format mailing address |
| | PhoneNumber | a USA format phone number |
| | EmailAddress | a standard email address |
| | ContactMechanism | a generic serialization wrapper for phones, emails, addresses |
| _**facets**_ | IContactService | defines web service API |

### Discussion

The _**domain**_ package in this library contains classes that demonstrate idiomatic JPA annotated domain model classes.
See the [diagram](#model-diagram) below to see how these parts relate to each other and the base types found in
the generic [educery-storage](../educery-storage) library.

To ensure uniqueness and facilitate fast searches, each model item is stored immutably along with a hash of its contents.
This way, when sought by value, the hash can again be generated and used to find the associated item.
For example, phone numbers and email addresses are kept unique.
Each different phone number and email address will have its own row in the backing store.
The same is true of mailing addresses, which are composed of a few distinct data elements.
Still, any difference in any of those values will produce a different mailing address entry in the backing store.

Mailing addresses also take advantage of the standard [bean validation][valid] framework.
The field values of a mailing address get checked against patterns and size constraints.
When the framework detects invalid data, it gets reported with an appropriate message.
These messages get reported to the calling client as violations of the API contract.

### Model Diagram
![Domain Model][model-diagram]

[model-diagram]: ../images/contacts-models.png
[composite]: ../educery-storage#composite-items
[spring-jpa]: https://spring.io/projects/spring-data-jpa
[valid]: https://en.wikipedia.org/wiki/Bean_Validation
