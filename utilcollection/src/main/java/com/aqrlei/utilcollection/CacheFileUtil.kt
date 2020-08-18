package com.aqrlei.utilcollection

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.aqrlei.utilcollection.ext.createDirs
import java.io.*

/**
 * created by AqrLei on 2020/7/21
 */
object CacheFileUtil {


    fun saveBitmap(bitmap: Bitmap, file: File, callback: (resultFile: File?) -> Unit) {
        if (file.isDirectory || !file.canWrite() || !file.exists()) {
            callback(null)
            return
        }
        val result = FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }

        callback(if (result) file else null)
    }

    fun isExternalStorageWritable(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun isExternalStorageReadable(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY

    /**
     * @param mimeType `image/ *`
     * @param relativePath DCIM/{your dir's name} and Pictures/{your dir's name}, android 10 之后生效
     * @param isPending 是否独占访问，android 10之后生效
     *
     */
    fun createMediaStoreImageUri(
        contentResolver: ContentResolver,
        displayName: String,
        mimeType: String,
        title: String,
        relativePath: String?=null,
        isPending: Int = 0): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.TITLE, title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                relativePath?.let{ put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)}
                put(MediaStore.Images.Media.IS_PENDING, isPending)
            }
        }


        val insertUri = if (isExternalStorageWritable()) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        }
        return contentResolver.insert(insertUri, values)

    }

    /**
     * @param mimeType `audio/ *`
     * @param relativePath Alarms/, Audiobooks/, Music/, Notifications/, Podcasts/, and Ringtones/
     * @param isPending 是否独占访问，android 10之后生效
     */
    fun createMediaStoreAudioUri(
        contentResolver: ContentResolver,
        displayName: String,
        mimeType: String,
        title: String,
        relativePath: String,
        isPending: Int = 0): Uri? {

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.TITLE, title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Audio.Media.IS_PENDING, isPending)
            }
        }
        val insertUri = if (isExternalStorageWritable()) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        }
        return contentResolver.insert(insertUri, values)

    }

    /**
     * @param mimeType `video/ *`
     * @param relativePath Movies/, and Pictures/, android 10 之后生效
     * @param isPending 是否独占访问，android 10之后生效
     */
    fun createMediaStoreVideoUri(
        contentResolver: ContentResolver,
        displayName: String,
        mimeType: String,
        title: String,
        relativePath: String,
        isPending: Int = 0): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.TITLE, title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Video.Media.IS_PENDING, isPending)
            }
        }
        val insertUri = if (isExternalStorageWritable()) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Video.Media.INTERNAL_CONTENT_URI
        }
        return contentResolver.insert(insertUri, values)
    }


    /**
     * 获取最大运存
     */
    fun runtimeMaxMemory() = Runtime.getRuntime().maxMemory()

    /**
     * 使用app下特定Cache文件夹，无需权限
     */
    fun getAppCacheDirFile(context: Context, uniqueName: String): File {
        val cachePath = context.externalCacheDir?.path ?: context.cacheDir.path

        return File("$cachePath${File.separator}$uniqueName").createDirs()
    }

    /**
     *  使用app下特定文件夹，无需权限
     *  @param customDirName 自定义文件夹的名子，可为空
     */
    @JvmStatic
    fun getAppFilesDirFile(context: Context, customDirName: String?): File {
        return (context.getExternalFilesDir(customDirName)
            ?: File("${context.filesDir}${File.separator}$customDirName")).createDirs()
    }

    @JvmStatic
    fun writeToAppFile(
        context: Context, fileName: String,
        callback: (fos: FileOutputStream) -> Unit,
        onError: ((e: Throwable) -> Unit)? = null) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use(callback)
        } catch (e: Exception) {
            onError?.invoke(e)
        }

    }

    @JvmStatic
    fun readFromAppFile(
        context: Context,
        fileName: String,
        callback: (fis: FileInputStream) -> Unit,
        onError: ((e: Throwable) -> Unit)? = null) {
        try {
            context.openFileInput(fileName).use(callback)
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    fun readFromAppRawFile(
        context: Context, rawId: Int,
        callback: (input: InputStream) -> Unit,
        onError: ((e: Throwable) -> Unit)? = null) {
        try {
            context.resources.openRawResource(rawId).use(callback)
        } catch (e: Exception) {
            onError?.invoke(e)
        }

    }

    /**
     * 最好在非UI线程中执行
     */
    @JvmStatic
    fun writeFileFromUri(context: Context, file: File, uri: Uri, callback: (file: File?) -> Unit) {
        if (file.isDirectory || !file.isFile || !file.canWrite()) return
        var ips: InputStream? = null
        var fileOps: FileOutputStream? = null
        try {
            ips = context.contentResolver.openInputStream(uri)
            ips ?: throw NullPointerException("InputStream is null")
            fileOps = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int
            while (ips.read(buffer).apply { len = this } > 0) {
                fileOps.write(buffer, 0, len)
            }
            fileOps.flush()
            callback(file)

        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        } finally {
            try {
                fileOps?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                ips?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun writeFileFromUri(context: Context, file: File, uri: Uri): File? {
        var resultFile: File?=null
        if (file.isDirectory || !file.isFile || !file.canWrite()) return resultFile

        var ips: InputStream? = null
        var fileOps: FileOutputStream? = null
        try {
            ips = context.contentResolver.openInputStream(uri)
            ips ?: throw NullPointerException("InputStream is null")
            fileOps = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int
            while (ips.read(buffer).apply { len = this } > 0) {
                fileOps.write(buffer, 0, len)
            }
            fileOps.flush()
            resultFile = file

        } catch (e: Exception) {
            e.printStackTrace()
            resultFile = null
        } finally {
            try {
                fileOps?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                ips?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return resultFile
    }

    /**
     * 要在非主线程中执行
     */
    @JvmStatic
    fun readFileFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
        callback: (file: ParcelFileDescriptor) -> Unit) {
        contentResolver.openFileDescriptor(uri, "r")?.use(callback)
    }

    /**
     *[Intent.ACTION_OPEN_DOCUMENT]
     * 想让应用获得对文档提供程序所拥有文档的长期、持续性访问权限，请使用 ACTION_OPEN_DOCUMENT。
     * 例如，照片编辑应用可让用户编辑存储在文档提供程序中的图像
     */
    @JvmStatic
    fun performFileSearch(
        activity: Activity?,
        fragment: Fragment?,
        reqCode: Int,
        mimeType: String = "*/*") {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE) // 只显示可打开的文档
            type = mimeType
        }
        if (fragment != null) {
            fragment.startActivityForResult(intent, reqCode)
        } else {
            activity?.startActivityForResult(intent, reqCode)
        }
    }

    /**
     * [Intent.ACTION_CREATE_DOCUMENT]
     */
    @JvmStatic
    fun createFile(
        activity: Activity?,
        fragment: Fragment?,
        mimeType: String,
        fileName: String,
        reqCode: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE) // 只显示可打开的文档
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        if (fragment != null) {
            fragment.startActivityForResult(intent, reqCode)
        } else {
            activity?.startActivityForResult(intent, reqCode)
        }
    }

    @JvmStatic
    fun deleteDocument(contentResolver: ContentResolver, uri: Uri): Boolean {
        var result: Boolean = false
        val isSupportDelete = isDocumentSupportDelete(contentResolver, uri)

        if (isSupportDelete) {
            result = DocumentsContract.deleteDocument(contentResolver, uri)
        }
        return result
    }

    @JvmStatic
    fun isDocumentSupportDelete(contentResolver: ContentResolver, uri: Uri): Boolean {
        val flags = dumpDocumentMetaData(
            contentResolver,
            uri,
            DocumentsContract.Document.COLUMN_FLAGS).toIntOrNull() ?: 0
        return (flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun isVirtualFile(contentResolver: ContentResolver, uri: Uri): Boolean {
        val flags = dumpDocumentMetaData(
            contentResolver,
            uri,
            DocumentsContract.Document.COLUMN_FLAGS).toIntOrNull() ?: 0

        return (flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0
    }

    @JvmStatic
    fun alertDocument(
        contentResolver: ContentResolver,
        uri: Uri,
        callback: (fileOutputStream: FileOutputStream) -> Unit) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fos ->
                    callback(fos)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    @JvmStatic
    fun dumpDocumentMetaData(
        contentResolver: ContentResolver,
        uri: Uri,
        columnName: String): String {
        val cursor = contentResolver.query(uri, null, null, null, null, null)
        var result: String = ""
        cursor?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(columnName))
            }
        }
        return result
    }

    /**
     * [Intent.ACTION_PICK]
     * 如果您只想让应用读取/导入数据，请使用 ACTION_GET_CONTENT
     */
    @JvmStatic
    fun pickFileItem(
        activity: Activity?,
        fragment: Fragment?,
        reqCode: Int,
        mimeType: String = "*/*",
        multiple: Boolean = false) {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
        if (fragment != null) {
            fragment.startActivityForResult(intent, reqCode)
        } else {
            activity?.startActivityForResult(intent, reqCode)
        }
    }

    /**
     * [Intent.ACTION_GET_CONTENT]
     */
    @JvmStatic
    fun getFileContent(
        activity: Activity?,
        fragment: Fragment?,
        reqCode: Int,
        vararg mimeType: String = arrayOf("*/*"),
        multiple: Boolean = false,
        localOnly: Boolean = false) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType(mimeType[0])
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, localOnly)
        if (fragment != null) {
            fragment.startActivityForResult(Intent.createChooser(intent,"Chooser"), reqCode)
        } else {
            activity?.startActivityForResult(Intent.createChooser(intent,"Chooser"), reqCode)
        }
    }
}