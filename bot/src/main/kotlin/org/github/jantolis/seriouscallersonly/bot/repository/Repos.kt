package org.github.jantolis.seriouscallersonly.bot.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface Repo<KeyType, ElementType> {
    suspend fun store(key: KeyType, element: ElementType)
    suspend fun find(key: KeyType): ElementType?
    suspend fun remove(key: KeyType)
    suspend fun clear()
}

class ConcurrentRepo<KeyType, ElementType>(
        private val backingRepo: Repo<KeyType, ElementType> = MapRepo()
) : Repo<KeyType, ElementType> {
    private val mtx = Mutex()

    override suspend fun store(key: KeyType, element: ElementType) = mtx.withLock {
        backingRepo.store(key, element)
    }

    override suspend fun find(key: KeyType) = mtx.withLock {
        backingRepo.find(key)
    }

    override suspend fun remove(key: KeyType): Unit = mtx.withLock {
        backingRepo.remove(key)
    }

    override suspend fun clear(): Unit = mtx.withLock {
        backingRepo.clear()
    }

}

class MapRepo<KeyType, ElementType> : Repo<KeyType, ElementType> {
    private val store = mutableMapOf<KeyType, ElementType>()

    override suspend fun store(key: KeyType, element: ElementType) {
        store[key] = element
    }

    override suspend fun find(key: KeyType) = store[key]

    override suspend fun remove(key: KeyType) {
        store.remove(key)
    }

    override suspend fun clear() {
        store.clear()
    }
}