package com.aqrlei.utilcollection.toast

import android.util.SparseArray
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.aqrlei.utilcollection.ext.isMainThread
import com.aqrlei.utilcollection.ext.runOnMainThread
import java.util.concurrent.atomic.AtomicBoolean

/**
 * created by AqrLei on 2020/3/17
 */
abstract class BaseToast : IToast {

    private var mToast: Toast? = null

    private val attachStateChangeMap = SparseArray< View.OnAttachStateChangeListener>()
    private val showTag = SparseArray<AtomicBoolean>()

    override fun longShow(message: String, type: Int?) {
        checkThreadShow(message, Toast.LENGTH_LONG, type)
    }

    override fun shortShow(message: String, type: Int?) {
        checkThreadShow(message, Toast.LENGTH_SHORT, type)
    }

    private fun checkThreadShow(message: String, duration: Int, type: Int?) {
        if (isMainThread()) {
            showInner(message, duration, type)
        } else {
            Runnable { showInner(message, duration, type) }.runOnMainThread()
        }
    }

    private fun showInner(message: String, duration: Int, type: Int?) {
        mToast = initToast(type)
        mToast?.let { toast ->
            assembleViewAttachStateChangeListener(toast)
            val isShow = showTag[toast.hashCode()]?.get() ?: false
            if (isShow) {
                toast.view?.findViewById<TextView>(getTextViewID(type))?.text = message
            } else {
                toast.duration = duration
                toast.view?.findViewById<TextView>(getTextViewID(type))?.text = message
                toast.show()
            }
        }
    }

    private fun assembleViewAttachStateChangeListener(toast: Toast) {
        val keyCode = toast.hashCode()
        if (attachStateChangeMap[keyCode] == null || showTag[keyCode] == null) {
            val showTagItem = showTag[keyCode] ?: AtomicBoolean(false).also {
                showTag.put(keyCode, it)
            }
            attachStateChangeMap[keyCode]?.let { oldListener ->
                toast.view?.removeOnAttachStateChangeListener(oldListener)
            }
            val attachStateChangeListener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    showTagItem.set(true)
                }

                override fun onViewDetachedFromWindow(v: View?) {
                    showTagItem.set(false)
                }
            }
            toast.view?.addOnAttachStateChangeListener(attachStateChangeListener)
            attachStateChangeMap.put(keyCode,attachStateChangeListener)
        }
    }

    abstract fun initToast(type: Int?): Toast

    abstract fun getTextViewID(type:Int?): Int
}