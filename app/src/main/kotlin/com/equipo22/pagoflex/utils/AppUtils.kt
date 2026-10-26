package com.equipo22.pagoflex.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.equipo22.pagoflex.MainActivity
import com.equipo22.pagoflex.R
import com.equipo22.pagoflex.data.CommitmentEntity
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.UserEntity
import java.io.File
import java.util.Calendar

fun createImageUri(context: Context, prefix: String = "comprobante"): Uri {
    val folder = File(context.cacheDir, "photos").apply { mkdirs() }
    val file = File(folder, "${prefix}_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                context.getString(R.string.channel_id),
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = context.getString(R.string.channel_description)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun show(context: Context, title: String, text: String, id: Int = System.currentTimeMillis().toInt()) {
        if (!hasPermission(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    fun checkDueAlerts(context: Context, user: UserEntity, commitments: List<CommitmentEntity>, companies: List<CompanyEntity>) {
        if (!user.avisosActivos) return
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 8) return
        val preferences = context.getSharedPreferences("pagoflex_alerts", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val today = nowDate()
        val companyMap = companies.associateBy { it.id }
        var shown = 0
        commitments
            .filter { it.estado == CommitmentStatus.PENDIENTE || it.estado == CommitmentStatus.VENCIDO }
            .sortedBy { it.fechaVencimiento }
            .forEach { commitment ->
                if (shown >= 3) return@forEach
                val remaining = daysUntil(commitment.fechaVencimiento)
                if (remaining in 0..user.diasAnticipacion) {
                    val key = "alert_${commitment.id}_$today"
                    if (!preferences.getBoolean(key, false)) {
                        val company = companyMap[commitment.companyId]?.nombreFantasia ?: "PagoFlex"
                        val message = if (remaining == 0) {
                            "Hoy vence ${commitment.concepto} de $company. Aún puedes pagarlo sin recargo."
                        } else {
                            "Te falta ${commitment.concepto} de $company. Vence el ${commitment.fechaVencimiento}."
                        }
                        show(context, "Un aviso para ti", message, commitment.id.toInt())
                        editor.putBoolean(key, true)
                        shown++
                    }
                }
            }
        editor.apply()
    }
}
