package com.example.data.sync

import android.content.Context
import androidx.work.*
import com.example.data.sync.PhotoSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import timber.log.Timber

class PhotoSyncService(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val syncWorkName = "PhotoSyncWork"
    private val syncWorkTag = "photo_sync_tag"

    fun schedulePeriodicSync(intervalMinutes: Long = 15) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<PhotoSyncWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(syncWorkTag)
            .build()

        workManager.enqueueUniquePeriodicWork(
            syncWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
        Timber.d("Scheduled periodic photo sync every $intervalMinutes minutes")
    }

    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<PhotoSyncWorker>()
            .setConstraints(constraints)
            .addTag(syncWorkTag)
            .build()

        workManager.enqueue(oneTimeWork)
        Timber.d("Triggered immediate photo sync")
    }

    fun cancelAllSync() {
        workManager.cancelAllWorkByTag(syncWorkTag)
        workManager.cancelUniqueWork(syncWorkName)
        Timber.d("Cancelled all photo sync work")
    }

    fun observeSyncStatus(): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData(syncWorkTag)
    }

    fun getPendingSyncCount(): Int {
        // This would need to be implemented via a synchronous call or flow
        return 0
    }

    fun isSyncRunning(): Boolean {
        // Check if any sync work is currently running
        return false // Simplified
    }
}

class PhotoSyncServiceProvider {
    companion object {
        @Volatile
        private var INSTANCE: PhotoSyncService? = null

        fun getInstance(context: Context): PhotoSyncService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PhotoSyncService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}