package com.aqrlei.utilcollection.ext

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * created by AqrLei on 2020/7/17
 */

/**
 * 对字符串进行md5编码
 */
fun String.toMd5(): String {
    return try {
        val sb = StringBuilder()
        val digest = MessageDigest.getInstance("MD5")
        digest.update(this.toByteArray())
        for (byte in digest.digest()) {
            val hex = Integer.toHexString(0Xff and byte.toInt())
            if (hex.length == 1) {
                sb.append("0")
            }
            sb.append(hex)
        }
        sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        this.hashCode().toString()
    }
}
