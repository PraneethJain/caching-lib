package com.cachinglib.policy

/**
 * Implements a First-In, First-Out (FIFO) eviction policy with O(1) operations.
 * The first key inserted is the first to be evicted.
 * Access order does not affect eviction order.
 */
class FifoEvictionPolicy[K] extends AbstractLinkedEvictionPolicy[K]:
  override def trackAccess(key: K): Unit = ()

  override def trackInsertion(key: K): Unit =
    if !nodeMap.contains(key) then
      val node = new Node(key, null, null)
      nodeMap.put(key, node)
      addNodeToEnd(node)

  override def evict(): Option[K] =
    val firstNode = head.next
    if firstNode eq tail then None
    else
      val key = firstNode.key
      trackRemoval(key)
      Some(key)
