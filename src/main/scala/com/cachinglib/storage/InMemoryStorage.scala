package com.cachinglib.storage

import scala.collection.mutable

/** A simple in-memory storage implementation using a mutable HashMap. Note:
  * This class itself is not thread-safe; thread safety is handled by the
  * `Cache` class that uses it.
  */
class InMemoryStorage[K, V] extends Storage[K, V]:
  private val map = mutable.Map.empty[K, V]

  override def get(key: K): Option[V] = map.get(key)
  override def put(key: K, value: V): Unit = map.put(key, value)
  override def remove(key: K): Option[V] = map.remove(key)
  override def contains(key: K): Boolean = map.contains(key)
  override def size: Int = map.size
  override def clear(): Unit = map.clear()
