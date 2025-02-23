/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.withTimeout

/**
 * Class responsible for postponing call until the socket connection is established.
 *
 * @param userScope Coroutine scope where the call should be run.
 * @param timeoutInMillis Timeout time in milliseconds.
 * @param awaitConnection Function which awaits a WS connection being established.
 */
internal class CallPostponeHelper(
    private val userScope: UserScope,
    private val timeoutInMillis: Long = DEFAULT_TIMEOUT,
    private val awaitConnection: suspend () -> Unit,
) {

    private val logger = StreamLog.getLogger("Chat:CallPostponeHelper")

    /**
     * Postpones or immediately executes the call based on [shouldPostpone] parameter.
     *
     * @param shouldPostpone Whether the call should be postponed
     * @param call A call to be run when the socket connection is established.
     *
     * @return Executable async [Call] responsible for querying channels
     */
    internal fun <T : Any> postponeCallIfNeeded(shouldPostpone: Boolean, call: () -> Call<T>): Call<T> {
        return if (shouldPostpone) {
            postponeCall(call)
        } else {
            call()
        }
    }

    /**
     * Postpones call.
     *
     * @param call A call to be run when the socket connection is established.
     *
     * @return Executable async [Call] responsible for querying channels
     */
    @Suppress("TooGenericExceptionCaught")
    internal fun <T : Any> postponeCall(call: () -> Call<T>): Call<T> {
        return CoroutineCall(userScope) {
            try {
                logger.d { "[postponeCall] no args" }
                withTimeout(timeoutInMillis) {
                    awaitConnection()
                }
                logger.v { "[postponeCall] wait completed" }
                call().await()
            } catch (e: Throwable) {
                logger.e { "[postponeCall] failed: $e" }
                Result.error(
                    ChatError(
                        message = "Failed to perform call. Waiting for WS connection was too long."
                    )
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 5_000L
    }
}
