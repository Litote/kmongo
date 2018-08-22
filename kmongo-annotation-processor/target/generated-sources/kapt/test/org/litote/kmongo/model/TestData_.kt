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
import org.litote.kmongo.model.other.SimpleReferencedData_
import org.litote.kmongo.model.other.SimpleReferencedData_Col
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

open class TestData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestData?>) : KPropertyPath<T, TestData?>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.collections.List<kotlin.Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestData::name)

    val date: KPropertyPath<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Date?>(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<java.util.Locale>?, kotlin.collections.Set<kotlin.String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Float?>(this,TestData::nullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,TestData::nullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,org.litote.kmongo.property.findProperty<TestData,String?>("privateData"))

    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<*>?>(this,TestData::id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.ByteArray?>(this,TestData::byteArray)
    companion object {
        val Set: SimpleReferencedData_Col<TestData>
            get() = SimpleReferencedData_Col<TestData>(null,TestData::set)
        val List: KCollectionSimplePropertyPath<TestData, List<Boolean>?>
            get() = KCollectionSimplePropertyPath(null, TestData::list)
        val Name: KProperty1<TestData, String?>
            get() = TestData::name
        val Date: KProperty1<TestData, Date?>
            get() = TestData::date
        val Referenced: SimpleReferencedData_<TestData>
            get() = SimpleReferencedData_<TestData>(null,TestData::referenced)
        val Map: KMapSimplePropertyPath<TestData, Id<Locale>?, Set<String>?>
            get() = KMapSimplePropertyPath(null, TestData::map)
        val Map2: SimpleReferenced2Data_Map<TestData, Locale>
            get() = SimpleReferenced2Data_Map<TestData, Locale>(null,TestData::map2)
        val NullableFloat: KProperty1<TestData, Float?>
            get() = TestData::nullableFloat
        val NullableBoolean: KProperty1<TestData, Boolean?>
            get() = TestData::nullableBoolean
        val PrivateData: KProperty1<TestData, String?>
            get() = org.litote.kmongo.property.findProperty<TestData,String?>("privateData")
        val Id: KProperty1<TestData, Id<*>?>
            get() = TestData::id
        val ByteArray: KProperty1<TestData, ByteArray?>
            get() = TestData::byteArray}
}

open class TestData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestData>?>) : KCollectionPropertyPath<T, TestData?, TestData_<T>>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.collections.List<kotlin.Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestData::name)

    val date: KPropertyPath<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Date?>(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<java.util.Locale>?, kotlin.collections.Set<kotlin.String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Float?>(this,TestData::nullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,TestData::nullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,org.litote.kmongo.property.findProperty<TestData,String?>("privateData"))

    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<*>?>(this,TestData::id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.ByteArray?>(this,TestData::byteArray)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestData_<T> = TestData_(this, customProperty(this, additionalPath))}

open class TestData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, TestData>?>) : KMapPropertyPath<T, K, TestData?, TestData_<T>>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KCollectionSimplePropertyPath<T, List<Boolean>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.collections.List<kotlin.Boolean>?>(this,TestData::list)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestData::name)

    val date: KPropertyPath<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Date?>(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KMapSimplePropertyPath<T, Id<Locale>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<java.util.Locale>?, kotlin.collections.Set<kotlin.String>?>(this,TestData::map)

    val map2: SimpleReferenced2Data_Map<T, Locale>
        get() = SimpleReferenced2Data_Map(this,TestData::map2)

    val nullableFloat: KPropertyPath<T, Float?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Float?>(this,TestData::nullableFloat)

    val nullableBoolean: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,TestData::nullableBoolean)

    val privateData: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,org.litote.kmongo.property.findProperty<TestData,String?>("privateData"))

    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<*>?>(this,TestData::id)

    val byteArray: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.ByteArray?>(this,TestData::byteArray)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestData_<T> = TestData_(this, customProperty(this, additionalPath))}
