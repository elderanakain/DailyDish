package io.krugosvet.dailydish.android.reminder

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.krugosvet.dailydish.android.reminder.worker.ReminderWorker
import io.krugosvet.dailydish.android.service.PreferenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val REMINDER_WORK_ID = "reminder_work_id"

@FlowPreview
@ExperimentalCoroutinesApi
class ReminderService(
  private val workManager: WorkManager,
  private val preferenceService: PreferenceService
) {

  private val constrains: Constraints
    get() = Constraints.Builder()
      .setRequiresBatteryNotLow(true)
      .setRequiresDeviceIdle(true)
      .build()

  private val work: PeriodicWorkRequest
    get() = PeriodicWorkRequest
      .Builder(
        ReminderWorker::class.java,
        1, TimeUnit.DAYS
      )
      .setConstraints(constrains)
      .build()

  fun schedule() = GlobalScope.launch(Dispatchers.IO) {
    preferenceService.isReminderEnabled
      .collect { isEnabled ->
        if (isEnabled) startWorker() else stopWorker()
      }
  }

  private fun stopWorker() {
    workManager.cancelUniqueWork(REMINDER_WORK_ID)
  }

  private fun startWorker() {
    workManager.enqueueUniquePeriodicWork(
      REMINDER_WORK_ID, ExistingPeriodicWorkPolicy.KEEP, work
    )
  }
}
