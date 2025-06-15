package com.cachinglib.storage

/**
 * A trait defining the contract for the underlying key-value storage mechanism.
 *
 * @tparam K The type of the keys.
 * @tparam V The type of the values.
 */
trait Storage[K, V]:
  def get(key: K): Option[V]
  def put(key: K, value: V): Unit
  def remove(key: K): Option[V]
  def contains(key: K): Boolean
  def size: Int
  def clear(): Unit
