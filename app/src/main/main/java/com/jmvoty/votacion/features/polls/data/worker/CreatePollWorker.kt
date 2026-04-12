package com.jmvoty.votacion.features.polls.data.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jmvoty.votacion.features.polls.data.network.PollService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MultipartBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

@HiltWorker
class CreatePollWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pollService: PollService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        // 'options' aquí ya es un Array<String> (ej: ["Go", "Python"])
        val options = inputData.getStringArray("options") ?: return Result.failure()
        val imageUris = inputData.getStringArray("image_uris") ?: emptyArray()

        android.util.Log.d("DEBUG_VOTACION", "Worker iniciado - Titulo: '$title', Opciones: ${options.size}")

        return try {
            val titleString = title

            // CORRECCIÓN: Como 'options' es Array<String>, se usa it directamente (sin .text)
            val combinedOptions = options.joinToString(",")

            // 2. Imágenes (Se mantiene igual)
            val imageParts = imageUris.mapIndexed { index, uriString ->
                val uri = Uri.parse(uriString)
                val file = uriToFile(applicationContext, uri) ?: return@mapIndexed null
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }.filterNotNull()

            android.util.Log.d("DEBUG_VOTACION", "Enviando a API: $combinedOptions")

            // 3. Llamada a la API (Asegúrate que coincida con el nombre de la variable 'combinedOptions')
            val response = if (imageParts.isEmpty()) {

                pollService.createPollSimple(titleString, combinedOptions)
            } else {
                pollService.createPollWithImages(titleString, combinedOptions, imageParts)
            }

            if (response.isSuccessful) {
                android.util.Log.d("DEBUG_VOTACION", "¡Logrado! Servidor respondió: ${response.code()}")
                Result.success()
            } else {
                val errorMsg = response.errorBody()?.string()
                android.util.Log.e("DEBUG_VOTACION", "Error de servidor: $errorMsg")
                if (response.code() in 500..599) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_VOTACION", "Excepción en Worker: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }


    private fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) { null }
    }
}