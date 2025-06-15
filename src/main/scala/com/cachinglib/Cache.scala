package com.cachinglib

import com.cachinglib.policy.EvictionPolicy
import com.cachinglib.storage.{InMemoryStorage, Storage}

import java.util.concurrent.locks.ReentrantLock

/**
 * A thread-safe, general-purpose, in-memory cache.
 *
 * This class is designed to be highly extensible by delegating eviction logic
 * to a swappable `EvictionPolicy`.
 *
 * Use the `Cache.Builder` to construct an instance.
 *
 * @tparam K The type of the keys.
 * @tparam V The type of the values.
 */
class Cache[K, V] private (
  capacity: Int,
  policy: EvictionPolicy[K],
  storage: Storage[K, V]
):
  private val lock = new ReentrantLock()

  /**
   * Retrieves the value associated with a key.
   * Returns `None` if the key is not in the cache.
   * This operation is thread-safe and updates the eviction policy (e.g., for LRU).
   */
  def get(key: K): Option[V] =
    lock.lock()
    try
      val valueOpt = storage.get(key)
      if valueOpt.isDefined then policy.trackAccess(key)
      valueOpt
    finally
      lock.unlock()

  /**
   * Adds or updates a key-value pair in the cache.
   * If the cache is at capacity, it evicts an item based on the configured policy.
   * This operation is thread-safe.
   */
  def put(key: K, value: V): Unit =
    lock.lock()
    try
      val keyExisted = storage.contains(key)
      
      if !keyExisted && storage.size >= capacity then
        policy.evict().foreach { keyToEvict =>
          storage.remove(keyToEvict)
          policy.trackRemoval(keyToEvict)
        }

      storage.put(key, value)
      if keyExisted then
        policy.trackAccess(key)
      else
        policy.trackInsertion(key)
    finally
      lock.unlock()

  /**
   * Removes a key and its associated value from the cache.
   *
   * @return The removed value, or `None` if the key was not found.
   */
  def remove(key: K): Option[V] =
    lock.lock()
    try
      val removedValueOpt = storage.remove(key)
      if removedValueOpt.isDefined then policy.trackRemoval(key)
      removedValueOpt
    finally
      lock.unlock()

  /**
   * Returns the current number of items in the cache.
   */
  def size: Int =
    lock.lock()
    try
      storage.size
    finally
      lock.unlock()
      
  /**
   * Removes all items from the cache.
   */
  def clear(): Unit =
    lock.lock()
    try
      storage.clear()
      policy.clear()
    finally
      lock.unlock()

object Cache:
  /**
   * A fluent builder for creating `Cache` instances.
   */
  class Builder[K, V]:
    private var capacity: Option[Int] = None
    private var policy: Option[EvictionPolicy[K]] = None

    /**
     * Sets the maximum number of items the cache can hold.
     * This is a required parameter.
     */
    def withCapacity(c: Int): Builder[K, V] =
      require(c > 0, "Capacity must be positive.")
      this.capacity = Some(c)
      this

    /**
     * Sets the eviction policy for the cache.
     * This is a required parameter.
     */
    def withPolicy(p: EvictionPolicy[K]): Builder[K, V] =
      this.policy = Some(p)
      this

    /**
     * Builds and returns a new `Cache` instance.
     *
     * @throws IllegalStateException if capacity or policy have not been set.
     */
    def build(): Cache[K, V] =
      val finalCapacity = capacity.getOrElse(throw new IllegalStateException("Capacity must be set."))
      val finalPolicy = policy.getOrElse(throw new IllegalStateException("Eviction policy must be set."))
      new Cache(finalCapacity, finalPolicy, new InMemoryStorage[K, V]())
