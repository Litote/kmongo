package org.litote.kmongo.issue

import java.time.Month
import java.time.Year
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

private val ___id: KProperty1<RecordCollectionImpl1, Long?>
    get() = RecordCollectionImpl1::_id
private val __Year: KProperty1<RecordCollectionImpl1, Year?>
    get() = RecordCollectionImpl1::year
private val __Month: KProperty1<RecordCollectionImpl1, Month?>
    get() = RecordCollectionImpl1::month
private val __Records: KProperty1<RecordCollectionImpl1, List<out Record1?>?>
    get() = RecordCollectionImpl1::records
class RecordCollectionImpl1_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        RecordCollectionImpl1?>) : KPropertyPath<T, RecordCollectionImpl1?>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val year: KPropertyPath<T, Year?>
        get() = KPropertyPath(this,__Year)

    val month: KPropertyPath<T, Month?>
        get() = KPropertyPath(this,__Month)

    val records: KCollectionSimplePropertyPath<T, out Record1??>
        get() = KCollectionSimplePropertyPath(this,RecordCollectionImpl1::records)

    companion object {
        val _id: KProperty1<RecordCollectionImpl1, Long?>
            get() = ___id
        val Year: KProperty1<RecordCollectionImpl1, Year?>
            get() = __Year
        val Month: KProperty1<RecordCollectionImpl1, Month?>
            get() = __Month
        val Records: KCollectionSimplePropertyPath<RecordCollectionImpl1, out Record1??>
            get() = KCollectionSimplePropertyPath(null, __Records)}
}

class RecordCollectionImpl1_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<RecordCollectionImpl1>?>) : KCollectionPropertyPath<T, RecordCollectionImpl1?,
        RecordCollectionImpl1_<T>>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val year: KPropertyPath<T, Year?>
        get() = KPropertyPath(this,__Year)

    val month: KPropertyPath<T, Month?>
        get() = KPropertyPath(this,__Month)

    val records: KCollectionSimplePropertyPath<T, out Record1??>
        get() = KCollectionSimplePropertyPath(this,RecordCollectionImpl1::records)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): RecordCollectionImpl1_<T> =
            RecordCollectionImpl1_(this, customProperty(this, additionalPath))}

class RecordCollectionImpl1_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        RecordCollectionImpl1>?>) : KMapPropertyPath<T, K, RecordCollectionImpl1?,
        RecordCollectionImpl1_<T>>(previous,property) {
    val _id: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,___id)

    val year: KPropertyPath<T, Year?>
        get() = KPropertyPath(this,__Year)

    val month: KPropertyPath<T, Month?>
        get() = KPropertyPath(this,__Month)

    val records: KCollectionSimplePropertyPath<T, out Record1??>
        get() = KCollectionSimplePropertyPath(this,RecordCollectionImpl1::records)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): RecordCollectionImpl1_<T> =
            RecordCollectionImpl1_(this, customProperty(this, additionalPath))}
