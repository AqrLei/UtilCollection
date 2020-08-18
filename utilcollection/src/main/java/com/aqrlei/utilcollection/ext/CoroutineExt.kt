package com.aqrlei.utilcollection.ext

import kotlinx.coroutines.*
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * created by AqrLei on 2020/7/21
 */
suspend fun <T> awaitCancellable(block: suspend () -> T, cancelBlock: () -> Unit) =
    suspendCancellableCoroutine<T> { cancellableContinuation ->
        GlobalScope.launch(Dispatchers.IO) {
            try {
                cancellableContinuation.resumeWith(Result.success(block()))
            } catch (e: Exception) {
                cancellableContinuation.resumeWithException(e)
            }

            cancellableContinuation.invokeOnCancellation {
                try {
                    cancelBlock()
                } catch (e: Throwable) {
                    throw e
                }
            }
        }
    }

suspend fun <T> await(block: suspend () -> T) = suspendCoroutine<T> { continuation ->
    GlobalScope.launch(Dispatchers.IO) {
        try {
            continuation.resumeWith(Result.success(block()))
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

fun <T> coroutineRun(
    backgroundBlock: suspend () -> T,
    resultCallback: (result: T) -> Unit,
    errorCallback: ((e: Throwable) -> Unit)? = null): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        try {
            resultCallback(await(backgroundBlock))
        } catch (e: Exception) {
            errorCallback?.invoke(e)
        }
    }
}

fun <T> coroutineRunLazy(
    backgroundBlock: suspend () -> T,
    resultCallback: (result: T) -> Unit,
    errorCallback: ((e: Throwable) -> Unit)? = null): Job {
    return GlobalScope.launch(Dispatchers.Main, CoroutineStart.LAZY) {
        try {
            resultCallback(await(backgroundBlock))
        } catch (e: Exception) {
            errorCallback?.invoke(e)
        }
    }
}

fun <T> coroutineCancellableRun(
    backgroundBlock: suspend () -> T,
    cancelBlock: () -> Unit,
    resultCallback: (result: T) -> Unit,
    errorCallback: ((e: Throwable) -> Unit)? = null): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        try {
            resultCallback(awaitCancellable(backgroundBlock, cancelBlock))
        } catch (e: Exception) {
            errorCallback?.invoke(e)
        }
    }
}

fun <T> coroutineCancellableRunLazy(
    backgroundBlock: suspend () -> T,
    cancelBlock: () -> Unit,
    resultCallback: (result: T) -> Unit,
    errorCallback: ((e: Throwable) -> Unit)? = null): Job {
    return GlobalScope.launch(Dispatchers.Main, CoroutineStart.LAZY) {
        try {
            resultCallback(awaitCancellable(backgroundBlock, cancelBlock))
        } catch (e: Exception) {
            errorCallback?.invoke(e)
        }
    }
}