/*
 * Copyright 2010-2016 JetBrains s.r.o.
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
 * A number of common helper methods for writing unit tests. Copied from Kotlin repo.
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package kotlin.test

import kotlin.internal.InlineOnly
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KClass

/**
 * Current adapter providing assertion implementations
 */
val asserter: Asserter
    get() = _asserter ?: lookupAsserter()

/** Used to override current asserter internally */
internal var _asserter: Asserter? = null

/** Asserts that the given [block] returns `true`. */
fun assertTrue(message: String? = null, block: () -> Boolean): Unit = assertTrue(block(), message)

/** Asserts that the expression is `true` with an optional [message]. */
fun assertTrue(actual: Boolean, message: String? = null) {
    return asserter.assertTrue(message ?: "Expected value to be true.", actual)
}

/** Asserts that the given [block] returns `false`. */
fun assertFalse(message: String? = null, block: () -> Boolean): Unit = assertFalse(block(), message)

/** Asserts that the expression is `false` with an optional [message]. */
fun assertFalse(actual: Boolean, message: String? = null) {
    return asserter.assertTrue(message ?: "Expected value to be false.", !actual)
}

/** Asserts that the [expected] value is equal to the [actual] value, with an optional [message]. */
fun <@OnlyInputTypes T> assertEquals(expected: T, actual: T, message: String? = null) {
    asserter.assertEquals(message, expected, actual)
}

/** Asserts that the [actual] value is not equal to the illegal value, with an optional [message]. */
fun <@OnlyInputTypes T> assertNotEquals(illegal: T, actual: T, message: String? = null) {
    asserter.assertNotEquals(message, illegal, actual)
}

/** Asserts that [expected] is the same instance as [actual], with an optional [message]. */
fun <@OnlyInputTypes T> assertSame(expected: T, actual: T, message: String? = null) {
    asserter.assertSame(message, expected, actual)
}

/** Asserts that [actual] is not the same instance as [illegal], with an optional [message]. */
fun <@OnlyInputTypes T> assertNotSame(illegal: T, actual: T, message: String? = null) {
    asserter.assertNotSame(message, illegal, actual)
}

/** Asserts that the [actual] value is not `null`, with an optional [message]. */
fun <T : Any> assertNotNull(actual: T?, message: String? = null): T {
    asserter.assertNotNull(message, actual)
    return actual!!
}

/** Asserts that the [actual] value is not `null`, with an optional [message] and a function [block] to process the not-null value. */
fun <T : Any, R> assertNotNull(actual: T?, message: String? = null, block: (T) -> R) {
    asserter.assertNotNull(message, actual)
    if (actual != null) {
        block(actual)
    }
}

/** Asserts that the [actual] value is `null`, with an optional [message]. */
fun assertNull(actual: Any?, message: String? = null) {
    asserter.assertNull(message, actual)
}

/** Marks a test as having failed if this point in the execution path is reached, with an optional [message]. */
fun fail(message: String? = null): Nothing {
    asserter.fail(message)
}

/** Asserts that given function [block] returns the given [expected] value. */
fun <@OnlyInputTypes T> expect(expected: T, block: () -> T) {
    assertEquals(expected, block())
}

/** Asserts that given function [block] returns the given [expected] value and use the given [message] if it fails. */
fun <@OnlyInputTypes T> expect(expected: T, message: String?, block: () -> T) {
    assertEquals(expected, block(), message)
}

/** Asserts that given function [block] fails by throwing an exception. */
fun assertFails(block: () -> Unit): Throwable = assertFails(null, block)

/** Asserts that given function [block] fails by throwing an exception. */
@SinceKotlin("1.1")
fun assertFails(message: String?, block: () -> Unit): Throwable {
    try {
        block()
    } catch (e: Throwable) {
        assertEquals(e.message, e.message) // success path assertion for qunit
        return e
    }
    asserter.fail(messagePrefix(message) + "Expected an exception to be thrown, but was completed successfully.")
}

/** Asserts that a [block] fails with a specific exception of type [T] being thrown.
 *  Since inline method doesn't allow to trace where it was invoked, it is required to pass a [message] to distinguish this method call from others.
 */
@InlineOnly
inline fun <reified T : Throwable> assertFailsWith(message: String? = null, noinline block: () -> Unit) : T =
        assertFailsWith(T::class, message, block)

/** Asserts that a [block] fails with a specific exception of type [exceptionClass] being thrown. */
fun <T : Throwable> assertFailsWith(exceptionClass: KClass<T>, block: () -> Unit): T =
        assertFailsWith(exceptionClass, null, block)


// From AssertionsH.kt
/**
 * Comments out a block of test code until it is implemented while keeping a link to the code
 * to implement in your unit test output
 */
@Suppress("UNUSED_PARAMETER")
inline fun todo(block: () -> Unit) {
    println("TODO")
}

/** Asserts that a [block] fails with a specific exception of type [exceptionClass] being thrown. */
fun <T : Throwable> assertFailsWith(exceptionClass: KClass<T>, message: String?, block: () -> Unit): T {
    try {
        block()
    } catch (e: Throwable) {
        if (exceptionClass.isInstance(e)) {
            @Suppress("UNCHECKED_CAST")
            return e as T
        }

        @Suppress("INVISIBLE_MEMBER")
        asserter.fail(messagePrefix(message) + "Expected an exception of ${exceptionClass.qualifiedName} to be thrown, but was $e")
    }

    @Suppress("INVISIBLE_MEMBER")
    val msg = messagePrefix(message)
    asserter.fail(msg + "Expected an exception of ${exceptionClass.qualifiedName} to be thrown, but was completed successfully.")
}

// From Assertions.kt
/**
 * Abstracts the logic for performing assertions.
 */
interface Asserter {
    /**
     * Fails the current test with the specified message.
     *
     * @param message the message to report.
     */
    fun fail(message: String?): Nothing

    /**
     * Asserts that the specified value is `true`.
     *
     * @param lazyMessage the function to return a message to report if the assertion fails.
     */
    fun assertTrue(lazyMessage: () -> String?, actual: Boolean): Unit {
        if (!actual) {
            fail(lazyMessage())
        }
    }

    /**
     * Asserts that the specified value is `true`.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertTrue(message: String?, actual: Boolean): Unit {
        assertTrue({ message }, actual)
    }

    /**
     * Asserts that the specified values are equal.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertEquals(message: String?, expected: Any?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Expected <$expected>, actual <$actual>." }, actual == expected)
    }

    /**
     * Asserts that the specified values are not equal.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertNotEquals(message: String?, illegal: Any?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Illegal value: <$actual>." }, actual != illegal)
    }

    /**
     * Asserts that the specified values are the same instance.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertSame(message: String?, expected: Any?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Expected <$expected>, actual <$actual> is not same." }, actual === expected)
    }

    /**
     * Asserts that the specified values are not the same instance.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertNotSame(message: String?, illegal: Any?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Expected not same as <$actual>." }, actual !== illegal)
    }

    /**
     * Asserts that the specified value is `null`.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertNull(message: String?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Expected value to be null, but was: <$actual>." }, actual == null)
    }

    /**
     * Asserts that the specified value is not `null`.
     *
     * @param message the message to report if the assertion fails.
     */
    fun assertNotNull(message: String?, actual: Any?): Unit {
        assertTrue({ messagePrefix(message) + "Expected value to be not null." }, actual != null)
    }

}

/**
 * Checks applicability and provides Asserter instance
 */
interface AsserterContributor {
    /**
     * Provides [Asserter] instance or `null` depends on the current context.
     *
     * @return asserter instance or null if it is not applicable now
     */
    fun contribute(): Asserter?
}

