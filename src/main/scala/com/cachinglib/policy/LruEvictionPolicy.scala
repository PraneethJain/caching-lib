package com.cachinglib.policy

/** Implements a Least Recently Used (LRU) eviction policy. Evicts the key that
  * has not been accessed for the longest time.
  */
class LruEvictionPolicy[K] extends AbstractLinkedEvictionPolicy[K]:
  override def trackAccess(key: K): Unit =
    nodeMap.get(key).foreach { node =>
      removeNode(node)
      addNodeToFront(node)
    }

  override def trackInsertion(key: K): Unit =
    if nodeMap.contains(key) then trackAccess(key)
    else
      val newNode = new Node(key, null, null)
      nodeMap.put(key, newNode)
      addNodeToFront(newNode)

  override def evict(): Option[K] =
    val lruNode = tail.prev
    if lruNode eq head then None
    else
      val keyToEvict = lruNode.key
      trackRemoval(keyToEvict)
      Some(keyToEvict)
