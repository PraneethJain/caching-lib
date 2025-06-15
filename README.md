# Caching Library

An extensible, thread-safe, in-memory caching library for Scala. Supports pluggable eviction policies (LRU, FIFO, LIFO) and custom storage backends.

## Usage

### Add to your project
Clone this repository and use the source directly, or copy the relevant files into your project.

### Example
```scala
import com.cachinglib.Cache
import com.cachinglib.policy.LruEvictionPolicy

val cache = new Cache.Builder[String, Int]()
  .withCapacity(100)
  .withPolicy(new LruEvictionPolicy[String]())
  .build()

cache.put("foo", 42)
println(cache.get("foo")) // Some(42)
```

### Using a custom storage
```scala
import com.cachinglib.storage.Storage

class MyCustomStorage[K, V] extends Storage[K, V] { /* ... */ }

val cache = new Cache.Builder[String, Int]()
  .withCapacity(100)
  .withPolicy(new LruEvictionPolicy[String]())
  .withStorage(new MyCustomStorage[String, Int]())
  .build()
```

## Building
This project uses [sbt](https://www.scala-sbt.org/).

To compile:
```bash
sbt compile
```

## Testing
To run all tests:
```bash
sbt test
```

## Project Structure
- `src/main/scala/com/cachinglib/Cache.scala` - Main cache implementation
- `src/main/scala/com/cachinglib/policy/` - Eviction policies (LRU, FIFO, LIFO, base class)
- `src/main/scala/com/cachinglib/storage/` - Storage abstraction and in-memory implementation
- `src/test/scala/com/cachinglib/` - Unit and concurrency tests

## Extending
- **Eviction Policy**: Implement the `EvictionPolicy[K]` trait or extend `AbstractLinkedEvictionPolicy[K]` for O(1) policies.
- **Storage**: Implement the `Storage[K, V]` trait for custom backends.

