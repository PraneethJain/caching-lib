package com.cachinglib.policy

import scala.collection.mutable

/**
 * Implements a Last-In, First-Out (LIFO) or stack-based eviction policy.
 * The most recently inserted key is the first to be evicted.
 * Access order does not affect eviction order.
 */
class LifoEvictionPolicy[K] extends EvictionPolicy[K]:
  private val stack = mutable.Stack.empty[K]

  override def trackAccess(key: K): Unit = () // No-op for LIFO

  override def trackInsertion(key: K): Unit = stack.push(key)

  override def trackRemoval(key: K): Unit = stack.filterInPlace(_ != key)

  override def evict(): Option[K] =
    if stack.nonEmpty then Some(stack.pop()) else None

  override def clear(): Unit = stack.clear()
