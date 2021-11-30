### Overview

| Package | Type | Summary |
|:------- |:---- |:------- |
| _**codecs**_ | ModelCodec | converts a model item to and from JSON |
| | ValueMap | accepts generic JSON without a known schema or model |
| _**context**_ | SpringContext | loads beans from a context defined in a local file |
| _**crypto**_ | Symmetric | encrypts and decrypts data with AES |
| | SecurityToken | a cryptographically secured payload |
| | LongHash | generates a long hash of bytes or text data |
| _**utils**_ | _Logging_ | grafts standard logging methods onto any class |
| | Utils | **static** convenience methods for collections and streams |
| | Exceptional | **static** convenience methods for executing closures with exceptions |
| _**validations**_ | ModelValidator | validates a model item using the bean validation framework |

### Discussion

**LongHash** takes its implementation from a [strong 64-bit hash function][long-hash].
A strong hash function is one that produces hash codes with good **dispersal**.

[long-hash]: https://www.javamex.com/tutorials/collections/strong_hash_code.shtml
