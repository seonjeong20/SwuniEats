// MyApp.kt
package com.example.swunieats

import android.app.Application
import android.content.Context
import java.io.FileOutputStream


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 앱이 시작될 때 가장 먼저 할 일!
        copyPreloadedDatabase(this)
    }

    private fun copyPreloadedDatabase(context: Context) {
        val dbName = DBHelper.DB_NAME
        val destPath = context.getDatabasePath(dbName)
        if (!destPath.exists()) {
            context.assets.open(dbName).use { input ->
                FileOutputStream(destPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
