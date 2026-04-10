package com.jmvoty.votacion.features.polls.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jmvoty.votacion.features.polls.domain.usecase.SyncVotesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncVotesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncVotesUseCase: SyncVotesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = syncVotesUseCase()
        return if (result.isSuccess) Result.success() else Result.retry()
    }
}