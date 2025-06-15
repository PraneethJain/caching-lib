package com.cachinglib.policy

import scala.collection.mutable

/**
 * Implements a First-In, First-Out (FIFO) eviction policy.
 * The first key inserted is the first to be evicted.
 * Access order does not affect eviction order.
 */
class FifoEvictionPolicy[K] extends EvictionPolicy[K]:
  private val queue = mutable.Queue.empty[K]

  override def trackAccess(key: K): Unit = () // No-op for FIFO

  override def trackInsertion(key: K): Unit = queue.enqueue(key)

  override def trackRemoval(key: K): Unit = queue.filterInPlace(_ != key)

  override def evict(): Option[K] =
    if queue.nonEmpty then Some(queue.dequeue()) else None

  override def clear(): Unit = queue.clear()
