package org.litote.kmongo.model

import java.util.Date
import java.util.Locale
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Float
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.model.other.SimpleReferencedData_
import org.litote.kmongo.model.other.SimpleReferencedData_Col
import org.litote.kmongo.property.KPropertyPath

open class TestData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestData?>) : KPropertyPath<T, TestData?>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KProperty1<T, List<List<Boolean>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::list)

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::name)

    val date: KProperty1<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KProperty1<T, Map<Id<Locale>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::map)

    val nullableFloat: KProperty1<T, Float?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::nullableFloat)

    val nullableBoolean: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::nullableBoolean)

    val privateData: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,org.litote.kmongo.property.findProperty<TestData,String?>("privateData"))

    val id: KProperty1<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::id)

    val byteArray: KProperty1<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::byteArray)
    companion object {
        val Set: SimpleReferencedData_Col<TestData>
            get() = SimpleReferencedData_Col<TestData>(null,TestData::set)
        val List: KProperty1<TestData, List<List<Boolean>>?>
            get() = TestData::list
        val Name: KProperty1<TestData, String?>
            get() = TestData::name
        val Date: KProperty1<TestData, Date?>
            get() = TestData::date
        val Referenced: SimpleReferencedData_<TestData>
            get() = SimpleReferencedData_<TestData>(null,TestData::referenced)
        val Map: KProperty1<TestData, Map<Id<Locale>, Set<String>>?>
            get() = TestData::map
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

open class TestData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestData>?>) : KPropertyPath<T, Collection<TestData>?>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val list: KProperty1<T, List<List<Boolean>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::list)

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::name)

    val date: KProperty1<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KProperty1<T, Map<Id<Locale>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::map)

    val nullableFloat: KProperty1<T, Float?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::nullableFloat)

    val nullableBoolean: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::nullableBoolean)

    val privateData: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,org.litote.kmongo.property.findProperty<TestData,String?>("privateData"))

    val id: KProperty1<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::id)

    val byteArray: KProperty1<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::byteArray)
}
