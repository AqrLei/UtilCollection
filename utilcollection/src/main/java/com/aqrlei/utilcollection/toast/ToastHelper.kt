package com.aqrlei.utilcollection.toast

import com.aqrlei.utilcollection.ContextInitUtil

/**
 * created by AqrLei on 2020/3/17
 */
object ToastHelper {
    private var toast: IToast? = null

    fun initToast(toast: IToast) {
        this.toast = toast
    }

    @JvmStatic
    fun shortShow(message: String?) {
        val show = { msg: String -> toast?.shortShow(msg) }
        message?.takeIf { it.isNotEmpty() }?.let(show)
    }

    @JvmStatic
    fun shortShow(message: String?, type: Int) {
        val show = { msg: String -> toast?.shortShow(msg, type) }
        message?.takeIf { it.isNotEmpty() }?.let(show)
    }

    @JvmStatic
    fun shortShow(resId: Int) {
        shortShow(ContextInitUtil.getString(resId))
    }

    @JvmStatic
    fun shortShow(resId: Int, type: Int) {
        shortShow(ContextInitUtil.getString(resId), type)
    }


    @JvmStatic
    fun longShow(message: String?) {
        val show = { msg: String -> toast?.longShow(msg) }
        message?.takeIf { it.isNotEmpty() }?.let(show)
    }

    @JvmStatic
    fun longShow(message: String?, type: Int) {
        val show = { msg: String -> toast?.longShow(msg, type) }
        message?.takeIf { it.isNotEmpty() }?.let(show)
    }

    @JvmStatic
    fun longShow(resId: Int) {
        longShow(ContextInitUtil.getString(resId))
    }

    @JvmStatic
    fun longShow(resId: Int, type: Int) {
        longShow(ContextInitUtil.getString(resId), type)
    }
}