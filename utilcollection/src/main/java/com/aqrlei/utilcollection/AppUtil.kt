package com.aqrlei.utilcollection

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aqrlei.utilcollection.ext.mainHandler
import java.io.File

/**
 * created by AqrLei on 2020/3/18
 */
object AppUtil {

    /**
     * 获取剪贴板数据
     */
    fun checkClip(
        context: Context,
        delayTime: Long = 500,
        block: (clipData: ClipData?, clipDes: ClipDescription?) -> Unit) {
        mainHandler.postDelayed({
            (context.getSystemService(ContextThemeWrapper.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
                block(it.primaryClip, it.primaryClipDescription)
            }
        }, delayTime)
    }

    /**
     * 应用是否在前台
     */
    fun isForeground(context: Context): Boolean {
        return (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses?.let { appProcesses ->
            var result = false
            for (appProcess in appProcesses) {
                if (appProcess.processName == context.packageName) {
                    result =
                        appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    break
                }
            }
            result
        } ?: false
    }

    /**
     * 查询是否有Activity可以响应Intent的跳转
     *
     * @param context 上下文
     * @param intent  需要查询的Intent
     * @return
     */
    fun queryActivities(context: Context, intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }


    /**
     * 跳转到邮件相关的应用
     */
    fun toEmailApp(context: Context, email: String, errorAction: (Exception) -> Unit) {
        val intent = Intent().apply {
            action = Intent.ACTION_SENDTO
            data = Uri.parse("mailto:$email")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            errorAction(e)
        }
    }

    /**
     * 通过浏览器打开网页
     */
    fun toWebPage(context: Context, uri: String) {
        val intent = Intent()
        intent.data = Uri.parse(uri)
        intent.action = Intent.ACTION_VIEW
        context.startActivity(intent)
    }

    /**
     * 跳转到设置界面
     * @param activity
     * @param reqCode
     */
    fun toAppSetting(activity: Activity, reqCode: Int) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", activity.packageName, null))
        if (queryActivities(activity, intent)) {
            activity.startActivityForResult(intent, reqCode)
        }
    }


    /**
     * 发送短信
     * @param tel     电话uri
     * @param content 短信内容
     */
    fun toSendSms(context: Context, tel: Uri, content: String) {
        val intent = Intent(Intent.ACTION_SENDTO, tel)
        intent.putExtra("sms_body", content)
        if (queryActivities(context, intent)) {
            context.startActivity(intent)
        }
    }

    /**
     * 跳转到拨号界面
     */
    fun toDialApp(context: Context, tel: Uri) {
        val intent = Intent(Intent.ACTION_DIAL, tel)
        if (queryActivities(context, intent)) {
            context.startActivity(intent)
        }
    }

    /**
     * 跳转到市场app
     */
    fun toMarketApp(context: Context) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName))
        if (queryActivities(context, intent)) {
            context.startActivity(Intent.createChooser(intent, "请选择应用市场"))
        }
    }

    /**
     * 跳转到系统相机 ，不需要Camera权限
     * @param fileUri 拍摄图片存放的位置
     */
    fun toCameraApp(context: Activity, fileUri: Uri, reqCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)//指定拍摄图片后的文件存储位置
        if (queryActivities(context, intent)) {
            context.startActivityForResult(intent, reqCode)
        }
    }

    /**
     * 跳转到联系人
     */
    fun toContactApp(context: Activity, fragment: Fragment?, reqCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        if (queryActivities(context, intent)) {
            fragment?.startActivityForResult(intent, reqCode)
                ?: context.startActivityForResult(intent, reqCode)
        }
    }

    fun toCropPic(context: Activity, uri: Uri, size: Int, saveFile: File, reqCode: Int) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true")

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size)
        intent.putExtra("outputY", size)
        intent.putExtra("return-data", false)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveFile))

        val resultIntent = Intent.createChooser(intent, "请选择剪切程序")
        if (queryActivities(
                context,
                resultIntent
            )
        ) {
            context.startActivityForResult(resultIntent, reqCode)
        }
    }

    /**
     * 跳转到指定应用的首页
     * */
    fun showActivity(context: Context?, packageName: String) {
        val intent = context?.packageManager?.getLaunchIntentForPackage(packageName)
        context?.startActivity(intent)
    }

    /**
     * 跳转到指定应用的指定页面
     * */
    fun showActivity(context: Context?, packageName: String, activityDir: String) {
        context?.startActivity(
            Intent()
                .setComponent(ComponentName(packageName, activityDir))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun getAppVersion(context: Context): Int =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }

    fun copyToClipboard(text: String, context: Context): Boolean {
        return (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.run {
            this.setPrimaryClip(ClipData.newPlainText("Label", text))
            true
        } ?: false
    }

    @SuppressLint("MissingPermission")
    fun isNetworkConnected(context: Context): Boolean {
        val networkInfo =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?)?.activeNetworkInfo
                ?: return false
        return networkInfo.isConnected
    }

    fun getInfoFromTrace(tracePlace: Int, thread: Thread): String {
        return if (thread.stackTrace.size > tracePlace) {
            thread.stackTrace[tracePlace].toString()
        } else {
            "out of stackTrace's size"
        }
    }

    fun getActivity(context: Context?): AppCompatActivity? {
        var resultContext = context
        while (resultContext is ContextWrapper) {
            if (resultContext is AppCompatActivity) {
                return resultContext
            }
            resultContext = resultContext.baseContext
        }
        return null
    }

    /**
     * 重启应用
     * */
    fun restartApp(activity: Activity?) {
        activity?.run {
            val intent =
                this.baseContext.packageManager.getLaunchIntentForPackage(this.baseContext.packageName)
            val restartIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            (activity.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.run {
                this.set(AlarmManager.RTC, System.currentTimeMillis() + 200, restartIntent)
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }

    /**
     * 校验是否忽略电池优化
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(context: Context?): Boolean {
        var isIgnoring = false
        (context?.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.let { powerManager ->
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return isIgnoring
    }

    /**
     * 请求电池优化白名单
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestIgnoreBatteryOptimizations(context: Context?) {
        try {
            context?.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${context.packageName}"))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 请求电池优化白名单
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestIgnoreBatteryOptimizations(context: AppCompatActivity?, requestCode: Int) {
        try {
            context?.startActivityForResult(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${context.packageName}")), requestCode
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}