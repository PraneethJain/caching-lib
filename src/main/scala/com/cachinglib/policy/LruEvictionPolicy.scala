package com.cachinglib.policy

import scala.collection.mutable

/**
 * Implements a Least Recently Used (LRU) eviction policy.
 * Evicts the key that has not been accessed for the longest time.
 * Achieves O(1) time complexity for all operations using a combination
 * of a HashMap and a Doubly Linked List.
 */
class LruEvictionPolicy[K] extends EvictionPolicy[K]:
  private class Node(val key: K, var prev: Node, var next: Node)

  private val nodeMap = mutable.Map.empty[K, Node]

  // Sentinel nodes to simplify list manipulation and avoid null checks
  private val head = new Node(null.asInstanceOf[K], null, null)
  private val tail = new Node(null.asInstanceOf[K], head, null)
  head.next = tail

  override def trackAccess(key: K): Unit =
    nodeMap.get(key).foreach { node =>
      removeNode(node)
      addNodeToFront(node)
    }

  override def trackInsertion(key: K): Unit =
    // If key already exists, treat it as an access. Otherwise, add new node.
    if nodeMap.contains(key) then
      trackAccess(key)
    else
      val newNode = new Node(key, null, null)
      nodeMap.put(key, newNode)
      addNodeToFront(newNode)

  override def trackRemoval(key: K): Unit =
    nodeMap.get(key).foreach { node =>
      removeNode(node)
      nodeMap.remove(key)
    }

  override def evict(): Option[K] =
    val lruNode = tail.prev
    if lruNode eq head then
      None // List is empty
    else
      val keyToEvict = lruNode.key
      trackRemoval(keyToEvict)
      Some(keyToEvict)

  override def clear(): Unit =
    nodeMap.clear()
    head.next = tail
    tail.prev = head

  private def addNodeToFront(node: Node): Unit =
    val oldFirst = head.next
    head.next = node
    node.prev = head
    node.next = oldFirst
    oldFirst.prev = node

  private def removeNode(node: Node): Unit =
    val prevNode = node.prev
    val nextNode = node.next
    prevNode.next = nextNode
    nextNode.prev = prevNode
