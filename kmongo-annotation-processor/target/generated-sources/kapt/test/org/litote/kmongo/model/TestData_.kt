package org.litote.kmongo.model

import java.util.Date
import java.util.Locale
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Float
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.model.other.SimpleReferencedData
import org.litote.kmongo.model.other.SimpleReferencedData_
import org.litote.kmongo.model.other.SimpleReferencedData_Col
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Set: KProperty1<TestData, Set<SimpleReferencedData>?>
    get() = TestData::set
private val __List: KProperty1<TestData, List<List<Boolean>>?>
    get() = TestData::list
private val __Name: KProperty1<TestData, String?>
    get() = TestData::name
private val __Date: KProperty1<TestData, Date?>
    get() = TestData::date
private val __Referenced: KProperty1<TestData, SimpleReferencedData?>
    get() = TestData::referenced
private val __Map: KProperty1<TestData, Map<Id<Locale>, Set<String>>?>
    get() = TestData::map
private val __Map2: KProperty1<TestData, Map<Locale, SimpleReferenced2Data>?>
    get() = TestData::map2
private val __NullableFloat: KProperty1<TestData, Float?>
    get() = TestData::nullableFloat
private val __NullableBoolean: KProperty1<TestData, Boolean?>
    get() = TestData::nullableBoolean
private val __PrivateData: KProperty1<TestData, String?>
    get() = org.litote.kreflect.findProperty<TestData,String?>("privateData")
private val __Id: KProperty1<TestData, Id<*>?>
    get() = TestData::id
private val __ByteArray: KProperty1<TestData, ByteArray?>
    get() = TestData::byteArray
open class TestData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestData?>) :
        KPropertyPath<T, TestData?>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = KCollectionSimplePropertyPath<T, List<Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val date: KPropertyPath<T, Date?>
        get() = KPropertyPath<T, Date?>(this,__Date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = KPropertyPath<T, Float?>(this,__NullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__NullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__PrivateData)

    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath<T, Id<*>?>(this,__Id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath<T, ByteArray?>(this,__ByteArray)

    companion object {
        val Set: SimpleReferencedData_Col<TestData>
            get() = SimpleReferencedData_Col<TestData>(null,__Set)
        val List: KCollectionSimplePropertyPath<TestData, List<Boolean>?>
            get() = KCollectionSimplePropertyPath(null, __List)
        val Name: KProperty1<TestData, String?>
            get() = __Name
        val Date: KProperty1<TestData, Date?>
            get() = __Date
        val Referenced: SimpleReferencedData_<TestData>
            get() = SimpleReferencedData_<TestData>(null,__Referenced)
        val Map: KMapSimplePropertyPath<TestData, Id<Locale>?, Set<String>?>
            get() = KMapSimplePropertyPath(null, __Map)
        val Map2: SimpleReferenced2Data_Map<TestData, Locale>
            get() = SimpleReferenced2Data_Map<TestData, Locale>(null,__Map2)
        val NullableFloat: KProperty1<TestData, Float?>
            get() = __NullableFloat
        val NullableBoolean: KProperty1<TestData, Boolean?>
            get() = __NullableBoolean
        val PrivateData: KProperty1<TestData, String?>
            get() = __PrivateData
        val Id: KProperty1<TestData, Id<*>?>
            get() = __Id
        val ByteArray: KProperty1<TestData, ByteArray?>
            get() = __ByteArray}
}

open class TestData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<TestData>?>) : KCollectionPropertyPath<T, TestData?,
        TestData_<T>>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = KCollectionSimplePropertyPath<T, List<Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val date: KPropertyPath<T, Date?>
        get() = KPropertyPath<T, Date?>(this,__Date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = KPropertyPath<T, Float?>(this,__NullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__NullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__PrivateData)

    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath<T, Id<*>?>(this,__Id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath<T, ByteArray?>(this,__ByteArray)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestData_<T> = TestData_(this,
            customProperty(this, additionalPath))}

open class TestData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        TestData>?>) : KMapPropertyPath<T, K, TestData?, TestData_<T>>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = KCollectionSimplePropertyPath<T, List<Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val date: KPropertyPath<T, Date?>
        get() = KPropertyPath<T, Date?>(this,__Date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = KPropertyPath<T, Float?>(this,__NullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__NullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__PrivateData)

    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath<T, Id<*>?>(this,__Id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath<T, ByteArray?>(this,__ByteArray)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestData_<T> = TestData_(this,
            customProperty(this, additionalPath))}
