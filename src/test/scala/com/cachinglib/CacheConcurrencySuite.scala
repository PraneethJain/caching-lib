package com.cachinglib

import com.cachinglib.policy.LruEvictionPolicy
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.Random

class CacheConcurrencySuite extends AnyFunSuite with Matchers:

  test("Cache should be thread-safe under concurrent load") {
    val capacity = 100
    val cache = new Cache.Builder[Int, Int]()
      .withCapacity(capacity)
      .withPolicy(new LruEvictionPolicy())
      .build()

    val numOperations = 5000
    val keyRange = 200

    val tasks = (1 to numOperations).map { i =>
      Future {
        val key = Random.nextInt(keyRange)
        if Random.nextBoolean() then
          cache.put(key, i)
        else
          cache.get(key)
      }
    }

    // Await for all futures to complete
    val allFutures = Future.sequence(tasks)
    Await.result(allFutures, 10.seconds)

    // The main assertion is that the cache did not crash and its state is valid.
    // The size should never exceed the capacity.
    cache.size should be <= capacity
    println(s"Final cache size after concurrent test: ${cache.size}")
  }
