package com.example.lab5_20202269;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificacionMotivacionalReceiver extends BroadcastReceiver {

    private static final int NOTIFICACION_MOTIVACIONAL_ID = 10000;
    private static final String CHANNEL_MOTIVACIONAL = "motivacional_channel";

    // Recibí ayuda para usar el BroadcastReceiver y AlarmManager para notificaciones
    // Como es parte del uso de AlarmManager, recibí ayuda de la IA para reprogramar
    // alarmas automáticas, y detalles avanzados de configuración de notificaciones
    // y el manejo de un BroadcastReceiver personalizado.

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificacionMotivacional", "NOTIFICACIÓN MOTIVACIONAL RECIBIDA");

        if (intent == null) {
            Log.e("NotificacionMotivacional", "Intent es null");
            return;
        }

        // Verificar si las notificaciones motivacionales siguen activas
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean activas = preferences.getBoolean("notificacionesMotivacionalesActivas", false);

        if (!activas) {
            Log.d("NotificacionMotivacional", "Notificaciones motivacionales desactivadas, ignorando");
            return;
        }

        // Obtener datos
        String mensaje = intent.getStringExtra("mensaje");
        String frecuencia = intent.getStringExtra("frecuencia");

        Log.d("NotificacionMotivacional", "Datos recibidos:");
        Log.d("NotificacionMotivacional", "   Mensaje: '" + mensaje + "'");
        Log.d("NotificacionMotivacional", "   Frecuencia: '" + frecuencia + "' horas");

        // Validar datos
        if (mensaje == null || mensaje.isEmpty()) {
            mensaje = "¡Sigue adelante, tú puedes!"; // Mensaje por defecto
            Log.w("NotificacionMotivacional", "Usando mensaje por defecto");
        }

        // Mostrar la notificación
        mostrarNotificacionMotivacional(context, mensaje);

        // Programar la siguiente notificación
        if (frecuencia != null && !frecuencia.isEmpty()) {
            programarSiguienteNotificacionMotivacional(context, mensaje, frecuencia);
        }

        Log.d("NotificacionMotivacional", "Proceso completado");
    }

    private void mostrarNotificacionMotivacional(Context context, String mensaje) {
        Log.d("NotificacionMotivacional", "Mostrando notificación motivacional");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e("NotificacionMotivacional", " NotificationManager es null");
            return;
        }

        // Crear Intent para abrir la app al tocar la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICACION_MOTIVACIONAL_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MOTIVACIONAL)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mensaje)
                        .setBigContentTitle(" Mensaje Motivacional")
                        .setSummaryText("Mantente motivado"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);

        try {
            notificationManager.notify(NOTIFICACION_MOTIVACIONAL_ID, builder.build());
            Log.d("NotificacionMotivacional", "Notificación motivacional mostrada exitosamente");
        } catch (Exception e) {
            Log.e("NotificacionMotivacional", "Error al mostrar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void programarSiguienteNotificacionMotivacional(Context context, String mensaje, String frecuenciaStr) {
        try {
            int frecuencia = Integer.parseInt(frecuenciaStr);
            long intervaloMs = frecuencia * 60 * 60 * 1000L; // Convertir horas a milisegundos
            long tiempoSiguienteNotificacion = System.currentTimeMillis() + intervaloMs;

            Log.d("NotificacionMotivacional", "Programando siguiente notificación en " + frecuencia + " horas");

            // Crear Intent para la siguiente notificación
            Intent siguienteIntent = new Intent(context, NotificacionMotivacionalReceiver.class);
            siguienteIntent.putExtra("mensaje", mensaje);
            siguienteIntent.putExtra("frecuencia", frecuenciaStr);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICACION_MOTIVACIONAL_ID,
                    siguienteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoSiguienteNotificacion, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoSiguienteNotificacion, pendingIntent);
                    }

                    Log.d("NotificacionMotivacional", " Siguiente notificación programada exitosamente");

                } catch (SecurityException e) {
                    Log.e("NotificacionMotivacional", " Error de permisos al programar siguiente notificación: " + e.getMessage());
                }
            } else {
                Log.e("NotificacionMotivacional", " AlarmManager es null");
            }

        } catch (NumberFormatException e) {
            Log.e("NotificacionMotivacional", " Error al parsear frecuencia: " + e.getMessage());
        }
    }
}