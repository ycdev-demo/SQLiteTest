package me.ycdev.android.demo.sqlite.utils

import me.ycdev.android.lib.common.async.AsyncTaskQueue

object AsyncHelper {
    private val taskQueue = AsyncTaskQueue("AsyncHelper")

    fun addTask(task: Runnable) {
        taskQueue.addTask(task)
    }
}
