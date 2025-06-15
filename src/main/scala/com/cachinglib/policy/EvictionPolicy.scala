package com.cachinglib.policy

/**
 * A trait defining the contract for an eviction policy.
 * Implementations of this trait determine which key to remove when the cache is full.
 *
 * @tparam K The type of the keys.
 */
trait EvictionPolicy[K]:
  /**
   * Records that a key was accessed (e.g., through a 'get' or 'put' update).
   * This is crucial for policies like LRU.
   */
  def trackAccess(key: K): Unit

  /**
   * Records that a new key was inserted into the cache.
   */
  def trackInsertion(key: K): Unit

  /**
   * Records that a key was removed from the cache, either manually or via eviction.
   * This allows the policy to clean up its internal tracking data.
   */
  def trackRemoval(key: K): Unit

  /**
   * Selects a key to be evicted according to the policy's rules.
   *
   * @return An Option containing the key to evict, or None if the policy is empty.
   */
  def evict(): Option[K]

  /**
   * Clears all tracking information from the policy.
   * Called when the cache itself is cleared.
   */
  def clear(): Unit
