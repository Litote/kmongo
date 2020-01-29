import org.junit.Test
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import kotlin.test.assertEquals

/*
 * Copyright (C) 2016/2020 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
class IdTest {

    @Test
    fun `cast works as expected`() {
        val id1: Id<String> = newId()
        val id2: Id<IdTest> = id1.cast()
        assertEquals<Id<*>>(id1, id2)
    }
}