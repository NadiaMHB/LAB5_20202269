package com.example.lab5_20202269;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lab5_20202269.databinding.ActivityMainBinding;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    private String fileName = "imagenSeleccionada.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        String nombreUsuario = preferences.getString("nombreUsuario", "Nadia");
        String mensajeMotivacional = preferences.getString("mensajeMotivacional", "¡Tú puedes lograrlo!");
        binding.tvSaludo.setText("¡Hola, " + nombreUsuario + "!");
        binding.tvMensaje.setText(mensajeMotivacional);

        // Mostrar imagen con Thread (segundo plano)
        cargarImagenEnSegundoPlano();

        // Elegir imagen
        binding.imageViewFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        binding.btnVerMedicamentos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicamentosActivity.class);
            startActivity(intent);
        });

        binding.btnConfiguraciones.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfiguracionesActivity.class);
            startActivity(intent);
        });

        crearCanalesNotificacion();

    }



    @Override
    protected void onResume() {
        super.onResume();
        // Cargar nombre y mensaje desde SharedPreferences
        String nombreUsuario = preferences.getString("nombreUsuario", "Nadia");
        String mensajeMotivacional = preferences.getString("mensajeMotivacional", "¡Tú puedes lograrlo!");
        binding.tvSaludo.setText("¡Hola, " + nombreUsuario + "!");
        binding.tvMensaje.setText(mensajeMotivacional);
    }

    // Para esta parte, tuve problemas pues al correr denuevo la aplicación, esta moría al intentar cargar la imagen,
    // por lo que decidí cargar la imagen en segundo plano usando un Thread. Recomendación de la IA
    private void cargarImagenEnSegundoPlano() {
        new Thread(() -> {
            try {
                FileInputStream fis = openFileInput(fileName);
                // Crear Drawable en segundo plano
                final android.graphics.drawable.Drawable imagen = android.graphics.drawable.Drawable.createFromStream(fis, null);
                fis.close();

                // Mostrar la imagen en el UI Thread
                new Handler(Looper.getMainLooper()).post(() -> binding.imageViewFoto.setImageDrawable(imagen));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //La imagen puede tardar en cargar si es muy grande,
    // ya que se usa InputStream y Drawable.createFromStream() :(

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
                int c;
                while ((c = inputStream.read()) != -1) {
                    fos.write(c);
                }
                fos.close();
                inputStream.close();

                // Cargar imagen nueva en segundo plano
                cargarImagenEnSegundoPlano();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void crearCanalesNotificacion() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String[] tipos = {"Pastilla", "Jarabe", "Ampolla", "Cápsula"};
            int[] importancias = {
                    NotificationManager.IMPORTANCE_HIGH,
                    NotificationManager.IMPORTANCE_DEFAULT,
                    NotificationManager.IMPORTANCE_LOW,
                    NotificationManager.IMPORTANCE_MIN
            };

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            for (int i = 0; i < tipos.length; i++) {
                String id = tipos[i];
                NotificationChannel channel = new NotificationChannel(
                        id,
                        "Canal " + tipos[i],
                        importancias[i]
                );
                channel.setDescription("Notificaciones para " + tipos[i]);
                channel.enableVibration(true); // Vibración habilitada
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
