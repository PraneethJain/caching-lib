package com.cachinglib

import com.cachinglib.policy.EvictionPolicy
import com.cachinglib.storage.{InMemoryStorage, Storage}

import java.util.concurrent.locks.ReentrantLock

/**
 * A thread-safe, general-purpose, in-memory cache.
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
   * 
   * @param key The key to look up.
   * @return An `Option` containing the value if found, or `None` if not.
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
   * 
   * @param key The key to add or update.
   * @param value The value to associate with the key.
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
   * @param key The key to remove.
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
   * A builder for creating `Cache` instances.
   */
  class Builder[K, V]:
    private var capacity: Option[Int] = None
    private var policy: Option[EvictionPolicy[K]] = None
    private var storage: Option[Storage[K, V]] = None

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
     * Sets the storage implementation for the cache.
     * Optional; defaults to InMemoryStorage if not set.
     */
    def withStorage(s: Storage[K, V]): Builder[K, V] =
      this.storage = Some(s)
      this

    /**
     * Builds and returns a new `Cache` instance.
     *
     * @throws IllegalStateException if capacity or policy have not been set.
     */
    def build(): Cache[K, V] =
      val finalCapacity = capacity.getOrElse(throw new IllegalStateException("Capacity must be set."))
      val finalPolicy = policy.getOrElse(throw new IllegalStateException("Eviction policy must be set."))
      val finalStorage = storage.getOrElse(new InMemoryStorage[K, V]())
      new Cache(finalCapacity, finalPolicy, finalStorage)
