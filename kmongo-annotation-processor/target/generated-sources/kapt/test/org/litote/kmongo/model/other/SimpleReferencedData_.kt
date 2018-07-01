package org.litote.kmongo.model.other

import kotlin.Int
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.model.SimpleReferenced2Data
import org.litote.kmongo.model.SimpleReferenced2Data_
import org.litote.kmongo.model.SubData
import org.litote.kmongo.model.SubData_
import org.litote.kmongo.model.TestData_
import org.litote.kmongo.property.KPropertyPath

class SimpleReferencedData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SimpleReferencedData?>) : KPropertyPath<T, SimpleReferencedData?>(previous,property) {
    val version: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SimpleReferencedData::version)

    val pojo2: SimpleReferenced2Data_<T>
        get() = SimpleReferenced2Data_(this,org.litote.kmongo.property.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))

    val pojo: TestData_<T>
        get() = TestData_(this,SimpleReferencedData::pojo)

    val subPojo: SubData_<T>
        get() = SubData_(this,org.litote.kmongo.property.findProperty<SimpleReferencedData,SubData>("subPojo"))
    companion object {
        val Version: KProperty1<SimpleReferencedData, Int?>
            get() = SimpleReferencedData::version
        val Pojo2: SimpleReferenced2Data_<SimpleReferencedData>
            get() = SimpleReferenced2Data_<SimpleReferencedData>(null,org.litote.kmongo.property.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))
        val Pojo: TestData_<SimpleReferencedData>
            get() = TestData_<SimpleReferencedData>(null,SimpleReferencedData::pojo)
        val SubPojo: SubData_<SimpleReferencedData>
            get() = SubData_<SimpleReferencedData>(null,org.litote.kmongo.property.findProperty<SimpleReferencedData,SubData>("subPojo"))}
}

class SimpleReferencedData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SimpleReferencedData>?>) : KPropertyPath<T, Collection<SimpleReferencedData>?>(previous,property) {
    val version: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SimpleReferencedData::version)

    val pojo2: SimpleReferenced2Data_<T>
        get() = SimpleReferenced2Data_(this,org.litote.kmongo.property.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))

    val pojo: TestData_<T>
        get() = TestData_(this,SimpleReferencedData::pojo)

    val subPojo: SubData_<T>
        get() = SubData_(this,org.litote.kmongo.property.findProperty<SimpleReferencedData,SubData>("subPojo"))
}
