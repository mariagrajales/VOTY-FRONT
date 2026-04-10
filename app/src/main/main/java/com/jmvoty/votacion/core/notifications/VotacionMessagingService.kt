package com.jmvoty.votacion.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jmvoty.votacion.MainActivity
import com.jmvoty.votacion.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jmvoty.votacion.features.auth.domain.repository.AuthRepository
import com.jmvoty.votacion.features.polls.domain.repository.PollRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VotacionMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var pollRepository: PollRepository

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // 1. Sincronización (esto SIEMPRE se hace, para que la lista se actualice sola)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pollRepository.refreshPolls()
            } catch (e: Exception) { }
        }

        // 2. EXTRA: Solo mostramos la notificación visual si la app NO está en primer plano
        if (!MainActivity.isAppInForeground) {
            val title = message.data["title"] ?: "¡Nueva Encuesta!"
            val body = message.data["body"] ?: "Hay una nueva votación disponible."
            val pollId = message.data["poll_id"]

            showHighPriorityNotification(title, body, pollId)
        } else {
            // Opcional: Aquí podrías mostrar un Toast o un sonido leve
            // si quieres que el usuario sepa que llegó algo mientras está dentro.
        }
    }

    private fun showHighPriorityNotification(title: String, message: String, pollId: String?) {
        val channelId = "high_priority_polls_v2"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Configuración del canal (IMPORTANCE_HIGH para que salte en pantalla)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas de Encuestas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Canal para notificaciones emergentes de nuevas encuestas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. CREACIÓN DEL PENDING INTENT (Esto es lo que faltaba)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("poll_id", pollId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. Construcción de la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Al tocar la notificación normal
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true) // Esto fuerza el banner (Heads-up)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            authRepository.updateDeviceToken(token)
        }
    }
}
