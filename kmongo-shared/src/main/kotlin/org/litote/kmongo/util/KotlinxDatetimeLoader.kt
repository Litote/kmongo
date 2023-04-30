package org.litote.kmongo.util

object KotlinxDatetimeLoader {
    private var hasLoaded = false
    private var isExistInClassLoader = false

    /**
     * Call Class.forName multiple times can cause severe perf issue
     * if dependency kotlinx doesn't exist in project implement KMongo.
     * So we load it only on first time and return nothing if class not exist.
     */
    fun <T> loadKotlinxDateTime(loadSerializersAndDeserializers: () -> T, empty: () -> T): T {
        return try {
            if (!hasLoaded) {
                hasLoaded = true
                Class.forName("kotlinx.datetime.Instant")
                isExistInClassLoader = true
            }

            if (!isExistInClassLoader) {
                return empty()
            }

            loadSerializersAndDeserializers()
        } catch (e: ClassNotFoundException) {
            isExistInClassLoader = false
            empty()
        }
    }
}