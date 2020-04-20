package hr.from.josipantolis.seriouscallersonly.runtime.repo

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface Repo<KeyType, ElementType> {
    suspend fun store(element: ElementType)
    suspend fun find(key: KeyType): ElementType?
    suspend fun remove(key: KeyType)
    suspend fun clear()
    suspend fun isEmpty(): Boolean
    suspend fun all(): Collection<Pair<KeyType, ElementType>>
}

class ConcurrentRepo<KeyType, ElementType>(
    private val backingRepo: Repo<KeyType, ElementType>
) : Repo<KeyType, ElementType> {
    private val mtx = Mutex()

    override suspend fun store(element: ElementType) = mtx.withLock {
        backingRepo.store(element)
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

    override suspend fun isEmpty(): Boolean = mtx.withLock {
        backingRepo.isEmpty()
    }

    override suspend fun all() = mtx.withLock {
        backingRepo.all()
    }

}

class MapRepo<KeyType, ElementType>(
    val keyExtractor: (ElementType) -> KeyType
) : Repo<KeyType, ElementType> {
    private val store = mutableMapOf<KeyType, ElementType>()

    override suspend fun store(element: ElementType) {
        store[keyExtractor(element)] = element
    }

    override suspend fun find(key: KeyType) = store[key]

    override suspend fun remove(key: KeyType) {
        store.remove(key)
    }

    override suspend fun clear() {
        store.clear()
    }

    override suspend fun isEmpty() = store.isEmpty()

    override suspend fun all() = store.entries.map { it.key to it.value }
}