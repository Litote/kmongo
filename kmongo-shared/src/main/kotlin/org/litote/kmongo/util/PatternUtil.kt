/*
 * Copyright (C) 2016/2021 Litote
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

package org.litote.kmongo.util

import java.util.HashMap
import java.util.regex.Pattern

/**
 * Provides pattern options copied from mongo [PatternCodec].
 */
object PatternUtil {

    /**
     * Returns pattern options as string.
     */
    fun getOptionsAsString(pattern: Pattern): String {
        var flags = pattern.flags()
        val buf = StringBuilder()
        for (flag in RegexFlag.values()) {
            if (pattern.flags() and flag.javaFlag > 0) {
                buf.append(flag.flagChar)
                flags -= flag.javaFlag
            }
        }
        require(flags <= 0) { "some flags could not be recognized." }
        return buf.toString()
    }

    private const val GLOBAL_FLAG = 256

    private enum class RegexFlag(
        val javaFlag: Int,
        val flagChar: Char,
        val unsupported: String?
    ) {
        CANON_EQ(
            Pattern.CANON_EQ,
            'c',
            "Pattern.CANON_EQ"
        ),
        UNIX_LINES(Pattern.UNIX_LINES, 'd', "Pattern.UNIX_LINES"), GLOBAL(
            GLOBAL_FLAG,
            'g',
            null
        ),
        CASE_INSENSITIVE(
            Pattern.CASE_INSENSITIVE,
            'i',
            null
        ),
        MULTILINE(Pattern.MULTILINE, 'm', null), DOTALL(
            Pattern.DOTALL,
            's',
            "Pattern.DOTALL"
        ),
        LITERAL(
            Pattern.LITERAL,
            't',
            "Pattern.LITERAL"
        ),
        UNICODE_CASE(
            Pattern.UNICODE_CASE,
            'u',
            "Pattern.UNICODE_CASE"
        ),
        COMMENTS(Pattern.COMMENTS, 'x', null);

        companion object {
            private val BY_CHARACTER: MutableMap<Char, RegexFlag> = HashMap()
            fun getByCharacter(ch: Char): RegexFlag? {
                return BY_CHARACTER[ch]
            }

            init {
                for (flag in values()) {
                    BY_CHARACTER[flag.flagChar] = flag
                }
            }
        }

    }
}