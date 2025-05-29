package com.example.lab5_20202269;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.lab5_20202269.databinding.ActivityConfiguracionesBinding;

public class ConfiguracionesActivity extends AppCompatActivity {

    private ActivityConfiguracionesBinding binding;
    private SharedPreferences preferences;

    // PROMT: Constantes para para notificación  que me permita definir ID para binding
    private static final int NOTIFICACION_MOTIVACIONAL_ID = 10000;
    private static final String CHANNEL_MOTIVACIONAL = "motivacional_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfiguracionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        crearCanalMotivacional();

        // Cargar valores actuales de SharedPreferences
        String mensajeActual = preferences.getString("mensajeMotivacional", "");
        String frecuenciaActual = preferences.getString("frecuenciaMotivacionalHoras", "");

        binding.tvMensaje.setText(mensajeActual);
        binding.tvFrecuencia.setText(frecuenciaActual);

        binding.btnGuardar.setOnClickListener(v -> {
            if (validarYGuardarConfiguracion()) {
                // Enviar notificación inmediata de confirmación
                enviarNotificacionInmediata();

                // Programar las notificaciones regulares
                programarNotificacionesMotivacionales();

                finish();
            }
        });

    }

    //Promt: puedes ayudarme con detalles más avanzados como BigTextStyle, colores, iconos, categorías?
    private void enviarNotificacionInmediata() {
        String mensaje = binding.tvMensaje.getText().toString().trim();
        if (mensaje.isEmpty()) {
            mensaje = "¡Sigue adelante, tú puedes!"; // Mensaje por defecto
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crear Intent para abrir la app al tocar la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICACION_MOTIVACIONAL_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_MOTIVACIONAL)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mensaje)
                        .setBigContentTitle("Mensaje Motivacional")
                        .setSummaryText("Motívate"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICACION_MOTIVACIONAL_ID, builder.build());
        }
    }


    // Promt: igual puedes ayudarme con las configuraciones para vibración personalizada?
    private void crearCanalMotivacional() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel canalMotivacional = new NotificationChannel(
                    CHANNEL_MOTIVACIONAL,
                    "Mensajes Motivacionales",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalMotivacional.setDescription("Notificaciones con mensajes motivacionales personalizados");
            canalMotivacional.enableVibration(true);
            canalMotivacional.setVibrationPattern(new long[]{0, 250, 250, 250});

            notificationManager.createNotificationChannel(canalMotivacional);
        }
    }

    private boolean validarYGuardarConfiguracion() {
        String mensaje = binding.tvMensaje.getText().toString().trim();
        String frecuenciaStr = binding.tvFrecuencia.getText().toString().trim();

        // Validaciones
        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Ingrese un mensaje motivacional", Toast.LENGTH_SHORT).show();
            binding.tvMensaje.requestFocus();
            return false;
        }

        if (frecuenciaStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la frecuencia en horas", Toast.LENGTH_SHORT).show();
            binding.tvFrecuencia.requestFocus();
            return false;
        }

        try {
            int frecuencia = Integer.parseInt(frecuenciaStr);
            if (frecuencia <= 0) {
                Toast.makeText(this, "La frecuencia debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                binding.tvFrecuencia.requestFocus();
                return false;
            }
            if (frecuencia > 168) { // Más de una semana
                Toast.makeText(this, "La frecuencia no puede ser mayor a 168 horas (1 semana)", Toast.LENGTH_SHORT).show();
                binding.tvFrecuencia.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido para la frecuencia", Toast.LENGTH_SHORT).show();
            binding.tvFrecuencia.requestFocus();
            return false;
        }

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mensajeMotivacional", mensaje);
        editor.putString("frecuenciaMotivacionalHoras", frecuenciaStr);
        editor.putBoolean("notificacionesMotivacionalesActivas", true);
        editor.apply();

        Log.d("ConfiguracionMotivacional", "Configuración guardada:");
        Log.d("ConfiguracionMotivacional", "Mensaje: " + mensaje);
        Log.d("ConfiguracionMotivacional", "Frecuencia: " + frecuenciaStr + " horas");

        return true;
    }

    // En esta parte no sabía como hacer para programar la siguiente notificación como alarmas exactas
    // En el curso no nos enseñaron directamente con AlarmManager pero lo consideré como solución de la IA
    //  para cumplir con el requerimiento de programar notificaciones periódicas

    private void programarNotificacionesMotivacionales() {

        String mensaje = preferences.getString("mensajeMotivacional", "");
        String frecuenciaStr = preferences.getString("frecuenciaMotivacionalHoras", "");

        if (mensaje.isEmpty() || frecuenciaStr.isEmpty()) {
            Log.e("ConfiguracionMotivacional", "Datos incompletos para programar notificación");
            return;
        }

        try {
            int frecuencia = Integer.parseInt(frecuenciaStr);
            long intervaloMs = frecuencia * 60 * 60 * 1000L; // Convertir horas a milisegundos
            long tiempoPrimeraNotificacion = System.currentTimeMillis() + intervaloMs;

            // Crear Intent para el BroadcastReceiver
            Intent intent = new Intent(this, NotificacionMotivacionalReceiver.class);
            intent.putExtra("mensaje", mensaje);
            intent.putExtra("frecuencia", frecuenciaStr);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    NOTIFICACION_MOTIVACIONAL_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Verificar permisos para Android 12+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Se requiere permiso de alarmas exactas", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (alarmManager != null) {
                // Programar la primera notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoPrimeraNotificacion, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoPrimeraNotificacion, pendingIntent);
                }

                Log.d("ConfiguracionMotivacional", "Primera notificación motivacional programada para: " +
                        (tiempoPrimeraNotificacion - System.currentTimeMillis()) / 1000 / 60 / 60 + " horas");
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error al programar notificaciones", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Error: Faltan permisos para programar alarmas", Toast.LENGTH_LONG).show();
        }
    }

    }