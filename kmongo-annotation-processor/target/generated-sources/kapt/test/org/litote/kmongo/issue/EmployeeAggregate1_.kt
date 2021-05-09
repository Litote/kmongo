package org.litote.kmongo.issue

import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<EmployeeAggregate1, Long?>
    get() = EmployeeAggregate1::_id
private val __Records: KProperty1<EmployeeAggregate1, List<out RecordCollection1?>?>
    get() = EmployeeAggregate1::records
class EmployeeAggregate1_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        EmployeeAggregate1?>) : KPropertyPath<T, EmployeeAggregate1?>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val records: KCollectionSimplePropertyPath<T, out RecordCollection1??>
        get() = KCollectionSimplePropertyPath(this,EmployeeAggregate1::records)

    companion object {
        val _id: KProperty1<EmployeeAggregate1, Long?>
            get() = ___id
        val Records: KCollectionSimplePropertyPath<EmployeeAggregate1, out RecordCollection1??>
            get() = KCollectionSimplePropertyPath(null, __Records)}
}

class EmployeeAggregate1_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<EmployeeAggregate1>?>) : KCollectionPropertyPath<T, EmployeeAggregate1?,
        EmployeeAggregate1_<T>>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val records: KCollectionSimplePropertyPath<T, out RecordCollection1??>
        get() = KCollectionSimplePropertyPath(this,EmployeeAggregate1::records)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EmployeeAggregate1_<T> =
            EmployeeAggregate1_(this, customProperty(this, additionalPath))}

class EmployeeAggregate1_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        EmployeeAggregate1>?>) : KMapPropertyPath<T, K, EmployeeAggregate1?,
        EmployeeAggregate1_<T>>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val records: KCollectionSimplePropertyPath<T, out RecordCollection1??>
        get() = KCollectionSimplePropertyPath(this,EmployeeAggregate1::records)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EmployeeAggregate1_<T> =
            EmployeeAggregate1_(this, customProperty(this, additionalPath))}
