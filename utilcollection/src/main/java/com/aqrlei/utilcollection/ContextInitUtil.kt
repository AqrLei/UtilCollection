package com.aqrlei.utilcollection

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * created by AqrLei on 2020/7/21
 */
object ContextInitUtil {
    private lateinit var contextReference: WeakReference<Context>
    private lateinit var applicationReference: WeakReference<Application>

    val resources: Resources?
        get() = contextReference.get()?.resources

    @JvmStatic
    fun doInit(application: Application) {
        applicationReference = WeakReference(application)
        contextReference = WeakReference(application.applicationContext)
    }

    @JvmStatic
    fun getContext(): Context? {
        return contextReference.get()
    }

    @JvmStatic
    fun getApplication(): Application? {
        return applicationReference.get()
    }

    @JvmStatic
    fun getString(resId: Int, vararg args: Any): String? {
        return getContext()?.getString(resId, *args)
    }

    @JvmStatic
    fun getColor(resId: Int): Int? {
        return getContext()?.let { ContextCompat.getColor(it, resId) }
    }

    @JvmStatic
    fun getDimenPixel(resId: Int): Int? {
        return resources?.getDimensionPixelSize(resId)
    }
}