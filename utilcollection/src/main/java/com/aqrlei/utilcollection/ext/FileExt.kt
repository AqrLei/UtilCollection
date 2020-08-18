package com.aqrlei.utilcollection.ext

import java.io.File

/**
 * created by AqrLei on 2020/7/21
 */
fun File.createDirs(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}