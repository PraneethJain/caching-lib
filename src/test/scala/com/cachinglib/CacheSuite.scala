package com.cachinglib

import com.cachinglib.policy.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CacheSuite extends AnyFunSuite with Matchers:

  test("LRU Cache should evict the least recently used item") {
    val cache = new Cache.Builder[String, Int]()
      .withCapacity(3)
      .withPolicy(new LruEvictionPolicy())
      .build()

    cache.put("a", 1) // a
    cache.put("b", 2) // b, a
    cache.put("c", 3) // c, b, a
    cache.get("a")    // a, c, b (a is now most recent)

    cache.put("d", 4) // d, a, c (b is evicted)

    cache.size should be(3)
    cache.get("b") should be(None)
    cache.get("a") should be(Some(1))
    cache.get("c") should be(Some(3))
    cache.get("d") should be(Some(4))
  }

  test("FIFO Cache should evict the first inserted item") {
    val cache = new Cache.Builder[String, Int]()
      .withCapacity(3)
      .withPolicy(new FifoEvictionPolicy())
      .build()

    cache.put("a", 1) // a
    cache.put("b", 2) // a, b
    cache.put("c", 3) // a, b, c
    cache.get("a")    // Accessing 'a' does not change its eviction order

    cache.put("d", 4) // b, c, d (a is evicted)

    cache.size should be(3)
    cache.get("a") should be(None)
    cache.get("b") should be(Some(2))
  }

  test("LIFO Cache should evict the last inserted item") {
    val cache = new Cache.Builder[String, Int]()
      .withCapacity(3)
      .withPolicy(new LifoEvictionPolicy())
      .build()

    cache.put("a", 1) // a
    cache.put("b", 2) // a, b
    cache.put("c", 3) // a, b, c

    cache.put("d", 4) // a, b, d (c is evicted)

    cache.size should be(3)
    cache.get("c") should be(None)
    cache.get("d") should be(Some(4))
  }

  test("Cache clear should remove all elements") {
    val cache = new Cache.Builder[String, Int]()
      .withCapacity(5)
      .withPolicy(new LruEvictionPolicy())
      .build()
      
    cache.put("a", 1)
    cache.put("b", 2)
    cache.size should be(2)

    cache.clear()
    cache.size should be(0)
    cache.get("a") should be(None)
  }

  test("Updating an existing key should not increase size") {
    val cache = new Cache.Builder[String, Int]()
      .withCapacity(2)
      .withPolicy(new LruEvictionPolicy())
      .build()
      
    cache.put("a", 1)
    cache.put("a", 100)
    
    cache.size should be(1)
    cache.get("a") should be(Some(100))
  }
