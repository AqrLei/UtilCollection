package com.aqrlei.utilcollection.ext

import android.os.Looper

/**
 * created by AqrLei on 2020/3/17
 */
val mainHandler:android.os.Handler = android.os.Handler(Looper.getMainLooper())

/**
 * 是否是当前线程是主线程
 */
fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

/**
 * 切换到主线程中执行任务
 */
fun Runnable.runOnMainThread(){
    mainHandler.post(this)
}

/**
 * 切换到主线程中执行延迟任务
 */
fun Runnable.runOnMainThreadDelay(delayMillis:Long){
    mainHandler.postDelayed(this,delayMillis)
}

/**
 * 取消主线程中的该任务
 */
fun Runnable.removeCallbacksOnMainThread(){
    mainHandler.removeCallbacks(this)
}

/**
 * 获取方法调用的层级堆栈信息，最底层的调用在堆栈信息的最前面
 */
fun Thread.getTaskNameFromTrace(place: Int): String{
    if(stackTrace.size > place){
        stackTrace[place].let {
            return it.className + ":"+ it.lineNumber
        }
    }
    return "task" +"-" + System.currentTimeMillis()
}