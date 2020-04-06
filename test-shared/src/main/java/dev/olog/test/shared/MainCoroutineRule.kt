/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.olog.test.shared

import dev.olog.domain.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the main coroutines dispatcher to a [TestCoroutineDispatcher] for unit testing. A
 * [TestCoroutineDispatcher] provides control over the execution of coroutines.
 *
 * Declare it as a JUnit Rule:
 *
 * ```
 * @get:Rule
 * var mainCoroutineRule = MainCoroutineRule()
 * ```
 *
 * Use the test dispatcher variable to modify the execution of coroutines
 *
 * ```
 * // This pauses the execution of coroutines
 * mainCoroutineRule.testDispatcher.pauseDispatcher()
 * ...
 * // This resumes the execution of coroutines
 * mainCoroutineRule.testDispatcher.resumeDispatcher()
 * ...
 * // This executes the coroutines running on testDispatcher synchronously
 * mainCoroutineRule.runBlocking { }
 * ```
 */
class MainCoroutineRule(
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}

fun MainCoroutineRule.runBlockingTest(block: suspend (TestCoroutineDispatcher) -> Unit) = this.testDispatcher.runBlockingTest {
    block(testDispatcher)
}

val MainCoroutineRule.schedulers: Schedulers
    get() {
        return object : Schedulers {
            override val io: CoroutineDispatcher
                get() = testDispatcher
            override val cpu: CoroutineDispatcher
                get() = testDispatcher
            override val main: CoroutineDispatcher
                get() = testDispatcher
        }
    }