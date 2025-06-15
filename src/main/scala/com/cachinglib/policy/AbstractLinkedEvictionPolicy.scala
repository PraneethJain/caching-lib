package com.cachinglib.policy

import scala.collection.mutable

/** Abstract base class for eviction policies using a doubly-linked list and
  * hashmap for O(1) operations. Subclasses define how to handle access,
  * insertion, and eviction.
  */
abstract class AbstractLinkedEvictionPolicy[K] extends EvictionPolicy[K]:
  protected class Node(val key: K, var prev: Node, var next: Node)
  protected val nodeMap = mutable.Map.empty[K, Node]
  protected val head =
    new Node(null.asInstanceOf[K], null, null) // Sentinel head
  protected val tail =
    new Node(null.asInstanceOf[K], head, null) // Sentinel tail
  head.next = tail
  tail.prev = head

  override def trackRemoval(key: K): Unit =
    nodeMap.get(key).foreach { node =>
      removeNode(node)
      nodeMap.remove(key)
    }

  override def clear(): Unit =
    nodeMap.clear()
    head.next = tail
    tail.prev = head

  protected def addNodeToFront(node: Node): Unit =
    val oldFirst = head.next
    head.next = node
    node.prev = head
    node.next = oldFirst
    oldFirst.prev = node

  protected def addNodeToEnd(node: Node): Unit =
    val oldLast = tail.prev
    tail.prev = node
    node.next = tail
    node.prev = oldLast
    oldLast.next = node

  protected def removeNode(node: Node): Unit =
    val prevNode = node.prev
    val nextNode = node.next
    prevNode.next = nextNode
    nextNode.prev = prevNode
