package wardrobe.infrastructure.abstractions

interface ImageStorage {
    fun put(key: String, contentType: String, content: ByteArray)
    fun delete(key: String)
    fun deleteAll(keys: Collection<String>)
    fun urlFor(key: String): String
}