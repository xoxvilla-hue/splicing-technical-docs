package com.phanu.myapps.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

data class AppInfo(
    val packageName: String,
    val name: String
)

fun Context.getInstalledApps(): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    return packageManager
        .queryIntentActivities(intent, PackageManager.GET_META_DATA)
        .mapNotNull { ri ->
            try {
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    name = ri.loadLabel(packageManager).toString()
                )
            } catch (e: Exception) { null }
        }
        .filter { it.packageName != packageName }
        .sortedBy { it.name }
        .distinctBy { it.packageName }
}

fun Context.getAppIcon(packageName: String): Bitmap? {
    return try {
        packageManager.getApplicationIcon(packageName).toBitmap()
    } catch (e: Exception) { null }
}

fun Context.launchApp(packageName: String) {
    packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
}
