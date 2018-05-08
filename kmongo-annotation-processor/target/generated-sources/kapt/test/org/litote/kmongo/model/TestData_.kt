package org.litote.kmongo.model

import java.util.Date
import java.util.Locale
import kotlin.String
import kotlin.collections.Collection
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

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::name)

    val date: KProperty1<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KProperty1<T, Map<Id<Locale>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::map)
    companion object {
        val Set: SimpleReferencedData_Col<TestData>
            get() = SimpleReferencedData_Col<TestData>(null,TestData::set)
        val Name: KProperty1<TestData, String?>
            get() = TestData::name
        val Date: KProperty1<TestData, Date?>
            get() = TestData::date
        val Referenced: SimpleReferencedData_<TestData>
            get() = SimpleReferencedData_<TestData>(null,TestData::referenced)
        val Map: KProperty1<TestData, Map<Id<Locale>, Set<String>>?>
            get() = TestData::map}
}

open class TestData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestData>?>) : KPropertyPath<T, Collection<TestData>?>(previous,property) {
    val set: SimpleReferencedData_Col<T>
        get() = SimpleReferencedData_Col(this,TestData::set)

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::name)

    val date: KProperty1<T, Date?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::date)

    val referenced: SimpleReferencedData_<T>
        get() = SimpleReferencedData_(this,TestData::referenced)

    val map: KProperty1<T, Map<Id<Locale>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestData::map)
}
