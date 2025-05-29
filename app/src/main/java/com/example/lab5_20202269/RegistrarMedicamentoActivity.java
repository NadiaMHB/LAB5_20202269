package com.example.lab5_20202269;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.lab5_20202269.databinding.ActivityRegistrarMedicamentoBinding;
import com.example.lab5_20202269.model.Medicamento;
import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistrarMedicamentoActivity extends AppCompatActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private ActivityRegistrarMedicamentoBinding binding;
    private SharedPreferences preferences;
    private Gson gson;
    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";

    // Constantes para los canales de notificación
    private static final String CHANNEL_PASTILLA = "pastilla_channel";
    private static final String CHANNEL_JARABE = "jarabe_channel";
    private static final String CHANNEL_AMPOLLA = "ampolla_channel";
    private static final String CHANNEL_CAPSULA = "capsula_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarMedicamentoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        gson = new Gson();

        // Crear canales de notificación
        crearCanalesNotificacion();


        // Configurar Spinner
        String[] tipos = {"Pastilla", "Jarabe", "Ampolla", "Cápsula"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.tvTipo.setAdapter(adapter);

        // Configurar click en EditTextFechaHora
        // En onCreate:
        binding.tvFechaHora.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            DatePickerFragment datePicker = new DatePickerFragment();
            datePicker.show(fragmentManager, "datepicker");
        }); // <-- Agrega este paréntesis y punto y coma

        binding.buttonGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarMedicamento();
            }
        }); // <-- Agrega este punto y coma

// En verificarPermisosAlarma, elimina el paréntesis y llave extra al final:
        // <-- Así debe terminar la función
    }


    private boolean validarCampos() {
        String nombre = binding.tvNombre.getText().toString().trim();
        String dosis = binding.tvDosis.getText().toString().trim();
        String frecuencia = binding.tvFrecuencia.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del medicamento", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dosis.isEmpty()) {
            Toast.makeText(this, "Ingrese la dosis", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (frecuencia.isEmpty()) {
            Toast.makeText(this, "Ingrese la frecuencia en horas", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Seleccione fecha y hora", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int freq = Integer.parseInt(frecuencia);
            if (freq <= 0) {
                Toast.makeText(this, "La frecuencia debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Frecuencia inválida", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void guardarMedicamento() {
        String nombre = binding.tvNombre.getText().toString().trim();
        String tipo = binding.tvTipo.getSelectedItem().toString();
        String dosis = binding.tvDosis.getText().toString().trim();
        String frecuencia = binding.tvFrecuencia.getText().toString().trim();
        String fechaHora = fechaSeleccionada + " " + horaSeleccionada;

        Medicamento nuevo = new Medicamento(nombre, tipo, dosis, frecuencia, fechaHora);
        String json = gson.toJson(nuevo);

        int total = preferences.getInt("totalMedicamentos", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("medicamento_" + total, json);
        editor.putInt("totalMedicamentos", total + 1);
        editor.apply();

        // Programar notificación
        programarNotificacion(nuevo, total);

        Toast.makeText(this, "Medicamento guardado y recordatorio programado", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        fechaSeleccionada = day + "/" + (month + 1) + "/" + year;
        TimePickerFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "timepicker");
    }

    @Override
    public void onTimeSelected(int hour, int minute) {
        horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        binding.tvFechaHora.setText(fechaSeleccionada + " " + horaSeleccionada);
    }

    private void crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Canal para Pastillas
            NotificationChannel canalPastilla = new NotificationChannel(
                    CHANNEL_PASTILLA,
                    "Recordatorios de Pastillas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalPastilla.setDescription("Notificaciones para recordar tomar pastillas");
            canalPastilla.enableVibration(true);
            canalPastilla.setVibrationPattern(new long[]{0, 500, 250, 500});

            // Canal para Jarabes
            NotificationChannel canalJarabe = new NotificationChannel(
                    CHANNEL_JARABE,
                    "Recordatorios de Jarabes",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canalJarabe.setDescription("Notificaciones para recordar tomar jarabes");
            canalJarabe.enableVibration(true);
            canalJarabe.setVibrationPattern(new long[]{0, 300, 200, 300});

            // Canal para Ampollas
            NotificationChannel canalAmpolla = new NotificationChannel(
                    CHANNEL_AMPOLLA,
                    "Recordatorios de Ampollas",
                    NotificationManager.IMPORTANCE_LOW
            );
            canalAmpolla.setDescription("Notificaciones para recordar aplicar ampollas");
            canalAmpolla.enableVibration(true);
            canalAmpolla.setVibrationPattern(new long[]{0, 800, 400, 800});

            // Canal para Cápsulas
            NotificationChannel canalCapsula = new NotificationChannel(
                    CHANNEL_CAPSULA,
                    "Recordatorios de Cápsulas",
                    NotificationManager.IMPORTANCE_MIN
            );
            canalCapsula.setDescription("Notificaciones para recordar tomar cápsulas");
            canalCapsula.enableVibration(true);
            canalCapsula.setVibrationPattern(new long[]{0, 400, 300, 400});

            // Crear los canales
            notificationManager.createNotificationChannel(canalPastilla);
            notificationManager.createNotificationChannel(canalJarabe);
            notificationManager.createNotificationChannel(canalAmpolla);
            notificationManager.createNotificationChannel(canalCapsula);
        }
    }

    private void programarNotificacion(Medicamento medicamento, int idNotificacion) {
        // Verificar si se pueden programar alarmas exactas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "No se puede programar recordatorio: permiso denegado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            // Calcular el tiempo de la primera notificación (fecha y hora seleccionadas)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaHoraCompleta = medicamento.getFechaHoraInicio();
            Date fechaHora = sdf.parse(fechaHoraCompleta);

            if (fechaHora == null) {
                Toast.makeText(this, "Error al programar recordatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            long tiempoPrimeraNotificacion = fechaHora.getTime();

            // Verificar que la fecha no sea en el pasado
            if (tiempoPrimeraNotificacion <= System.currentTimeMillis()) {
                Toast.makeText(this, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear el Intent para el BroadcastReceiver
            Intent intent = new Intent(this, NotificacionReceiver.class);
            intent.putExtra("nombre", medicamento.getNombre());
            intent.putExtra("tipo", medicamento.getTipo());
            intent.putExtra("dosis", medicamento.getDosis());
            intent.putExtra("frecuencia", medicamento.getFrecuenciaHoras());
            intent.putExtra("idNotificacion", idNotificacion);

            // Determinar el canal según el tipo
            String canal = obtenerCanalPorTipo(medicamento.getTipo());
            intent.putExtra("canal", canal);

            // Debug logs
            Log.d("NotificacionDebug", "Programando notificación:");
            Log.d("NotificacionDebug", "Nombre: " + medicamento.getNombre());
            Log.d("NotificacionDebug", "Tipo: " + medicamento.getTipo());
            Log.d("NotificacionDebug", "Dosis: " + medicamento.getDosis());
            Log.d("NotificacionDebug", "Frecuencia: " + medicamento.getFrecuenciaHoras());
            Log.d("NotificacionDebug", "Canal: " + canal);
            Log.d("NotificacionDebug", "ID: " + idNotificacion);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    idNotificacion,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (alarmManager != null) {
                // Programar la primera notificación
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoPrimeraNotificacion, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoPrimeraNotificacion, pendingIntent);
                    }
                    Toast.makeText(this, "Recordatorio programado exitosamente", Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "Error: Falta permiso para alarmas exactas", Toast.LENGTH_LONG).show();
                    // Mostrar diálogo para ir a configuración
                    mostrarDialogoPermisos();
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar recordatorio: formato de fecha inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoPermisos() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Para programar recordatorios, ve a Configuración > Aplicaciones > " + getString(R.string.app_name) + " > Alarmas y recordatorios, y activa el permiso.")
                .setPositiveButton("Ir a configuración", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String obtenerCanalPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "pastilla":
                return CHANNEL_PASTILLA;
            case "jarabe":
                return CHANNEL_JARABE;
            case "ampolla":
                return CHANNEL_AMPOLLA;
            case "cápsula":
                return CHANNEL_CAPSULA;
            default:
                return CHANNEL_PASTILLA; // Canal por defecto
        }
    }
}