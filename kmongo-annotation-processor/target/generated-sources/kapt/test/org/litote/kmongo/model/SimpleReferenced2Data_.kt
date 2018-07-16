package org.litote.kmongo.model

import kotlin.Double
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class SimpleReferenced2Data_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SimpleReferenced2Data?>) : KPropertyPath<T, SimpleReferenced2Data?>(previous,property) {
    val price: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SimpleReferenced2Data::price)
    companion object {
        val Price: KProperty1<SimpleReferenced2Data, Double?>
            get() = SimpleReferenced2Data::price}
}

class SimpleReferenced2Data_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SimpleReferenced2Data>?>) : KCollectionPropertyPath<T, SimpleReferenced2Data?>(previous,property) {
    val price: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SimpleReferenced2Data::price)
}
