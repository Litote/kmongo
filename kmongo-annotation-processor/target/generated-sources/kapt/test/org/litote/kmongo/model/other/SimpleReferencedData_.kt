package org.litote.kmongo.model.other

import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.model.SimpleReferenced2Data
import org.litote.kmongo.model.SimpleReferenced2Data_
import org.litote.kmongo.model.SubData
import org.litote.kmongo.model.SubData_
import org.litote.kmongo.model.TestData
import org.litote.kmongo.model.TestData_
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Version: KProperty1<SimpleReferencedData, Int?>
    get() = SimpleReferencedData::version
private val __Pojo2: KProperty1<SimpleReferencedData, SimpleReferenced2Data?>
    get() = org.litote.kreflect.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2")
private val __Pojo: KProperty1<SimpleReferencedData, TestData?>
    get() = SimpleReferencedData::pojo
private val __SubPojo: KProperty1<SimpleReferencedData, SubData?>
    get() = org.litote.kreflect.findProperty<SimpleReferencedData,SubData>("subPojo")
private val __Labels: KProperty1<SimpleReferencedData, Map<Locale, List<String>>?>
    get() = SimpleReferencedData::labels
class SimpleReferencedData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        SimpleReferencedData?>) : KPropertyPath<T, SimpleReferencedData?>(previous,property) {
    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val pojo2: SimpleReferenced2Data_<T>
        get() =
                SimpleReferenced2Data_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))

    val pojo: TestData_<T>
        get() = TestData_(this,SimpleReferencedData::pojo)

    val subPojo: SubData_<T>
        get() =
                SubData_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SubData>("subPojo"))

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,SimpleReferencedData::labels)

    companion object {
        val Version: KProperty1<SimpleReferencedData, Int?>
            get() = __Version
        val Pojo2: SimpleReferenced2Data_<SimpleReferencedData>
            get() = SimpleReferenced2Data_<SimpleReferencedData>(null,__Pojo2)
        val Pojo: TestData_<SimpleReferencedData>
            get() = TestData_<SimpleReferencedData>(null,__Pojo)
        val SubPojo: SubData_<SimpleReferencedData>
            get() = SubData_<SimpleReferencedData>(null,__SubPojo)
        val Labels: KMapSimplePropertyPath<SimpleReferencedData, Locale?, List<String>?>
            get() = KMapSimplePropertyPath(null, __Labels)}
}

class SimpleReferencedData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<SimpleReferencedData>?>) : KCollectionPropertyPath<T, SimpleReferencedData?,
        SimpleReferencedData_<T>>(previous,property) {
    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val pojo2: SimpleReferenced2Data_<T>
        get() =
                SimpleReferenced2Data_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))

    val pojo: TestData_<T>
        get() = TestData_(this,SimpleReferencedData::pojo)

    val subPojo: SubData_<T>
        get() =
                SubData_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SubData>("subPojo"))

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,SimpleReferencedData::labels)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleReferencedData_<T> =
            SimpleReferencedData_(this, customProperty(this, additionalPath))}

class SimpleReferencedData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        SimpleReferencedData>?>) : KMapPropertyPath<T, K, SimpleReferencedData?,
        SimpleReferencedData_<T>>(previous,property) {
    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val pojo2: SimpleReferenced2Data_<T>
        get() =
                SimpleReferenced2Data_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SimpleReferenced2Data>("pojo2"))

    val pojo: TestData_<T>
        get() = TestData_(this,SimpleReferencedData::pojo)

    val subPojo: SubData_<T>
        get() =
                SubData_(this,org.litote.kreflect.findProperty<SimpleReferencedData,SubData>("subPojo"))

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,SimpleReferencedData::labels)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SimpleReferencedData_<T> =
            SimpleReferencedData_(this, customProperty(this, additionalPath))}
