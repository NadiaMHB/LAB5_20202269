package com.example.lab5_20202269;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificacionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Validar que el intent no sea null
        if (intent == null) {
            return;
        }

        // Obtener datos del medicamento con valores por defecto
        String nombre = intent.getStringExtra("nombre");
        String tipo = intent.getStringExtra("tipo");
        String dosis = intent.getStringExtra("dosis");
        String frecuencia = intent.getStringExtra("frecuencia");
        String canal = intent.getStringExtra("canal");
        int idNotificacion = intent.getIntExtra("idNotificacion", 0);

        // Validar datos críticos
        if (nombre == null || nombre.isEmpty()) {
            nombre = "Medicamento";
        }
        if (tipo == null || tipo.isEmpty()) {
            tipo = "Pastilla"; // Valor por defecto
        }
        if (dosis == null || dosis.isEmpty()) {
            dosis = "1 unidad";
        }
        if (canal == null || canal.isEmpty()) {
            canal = "pastilla_channel"; // Canal por defecto
        }

        // Mostrar la notificación
        mostrarNotificacion(context, nombre, tipo, dosis, canal, idNotificacion);

        // Programar la siguiente notificación según la frecuencia
        if (frecuencia != null && !frecuencia.isEmpty()) {
            programarSiguienteNotificacion(context, intent, idNotificacion);
        }
    }

    private void mostrarNotificacion(Context context, String nombre, String tipo, String dosis, String canal, int idNotificacion) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear Intent para abrir la app al tocar la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                idNotificacion,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Determinar acción y icono según el tipo
        String accion = obtenerAccionPorTipo(tipo, dosis);
        int icono = obtenerIconoPorTipo(tipo);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canal)
                .setSmallIcon(icono)
                .setContentTitle("Recordatorio: " + nombre)
                .setContentText(accion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(accion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Mostrar la notificación
        notificationManager.notify(idNotificacion, builder.build());
    }

    private void programarSiguienteNotificacion(Context context, Intent originalIntent, int idNotificacion) {
        try {
            String frecuenciaStr = originalIntent.getStringExtra("frecuencia");
            if (frecuenciaStr == null || frecuenciaStr.isEmpty()) {
                return;
            }

            int frecuenciaHoras = Integer.parseInt(frecuenciaStr);
            long siguienteNotificacion = System.currentTimeMillis() + (frecuenciaHoras * 60 * 60 * 1000L);

            // Crear nuevo Intent para la siguiente notificación
            Intent siguienteIntent = new Intent(context, NotificacionReceiver.class);
            siguienteIntent.putExtra("nombre", originalIntent.getStringExtra("nombre"));
            siguienteIntent.putExtra("tipo", originalIntent.getStringExtra("tipo"));
            siguienteIntent.putExtra("dosis", originalIntent.getStringExtra("dosis"));
            siguienteIntent.putExtra("frecuencia", originalIntent.getStringExtra("frecuencia"));
            siguienteIntent.putExtra("canal", originalIntent.getStringExtra("canal"));
            siguienteIntent.putExtra("idNotificacion", idNotificacion);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    idNotificacion + 10000, // ID diferente para evitar conflictos
                    siguienteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, siguienteNotificacion, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, siguienteNotificacion, pendingIntent);
                }
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private String obtenerAccionPorTipo(String tipo, String dosis) {
        // Validar parámetros de entrada
        if (tipo == null) {
            tipo = "pastilla"; // Valor por defecto
        }
        if (dosis == null || dosis.isEmpty()) {
            dosis = "1 unidad";
        }

        switch (tipo.toLowerCase().trim()) {
            case "pastilla":
                return "Tomar " + dosis + " pastilla(s)";
            case "jarabe":
                return "Tomar " + dosis + " de jarabe";
            case "ampolla":
                return "Aplicar " + dosis + " ampolla(s)";
            case "cápsula":
            case "capsula":
                return "Tomar " + dosis + " cápsula(s)";
            default:
                return "Tomar " + dosis;
        }
    }

    private int obtenerIconoPorTipo(String tipo) {
        // Validar parámetro de entrada
        if (tipo == null) {
            return R.drawable.ic_medicamento; // Icono por defecto
        }

        switch (tipo.toLowerCase().trim()) {
            case "pastilla":
                return R.drawable.ic_pill;
            case "jarabe":
                return R.drawable.ic_syrup;
            case "ampolla":
                return R.drawable.ic_injection;
            case "cápsula":
            case "capsula":
                return R.drawable.ic_capsule;
            default:
                return R.drawable.ic_medicamento;
        }
    }
}