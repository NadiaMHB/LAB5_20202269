package com.example.lab5_20202269;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Sí enseñado en Clase 03.2 (Diálogos)
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.lab5_20202269.databinding.ActivityMedicamentosBinding;
import com.example.lab5_20202269.model.Medicamento;
import com.example.lab5_20202269.model.MedicamentosAdapter;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class MedicamentosActivity extends AppCompatActivity {

    private ActivityMedicamentosBinding binding;
    private List<Medicamento> listaMedicamentos;
    private MedicamentosAdapter adapter;
    private SharedPreferences preferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicamentosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        gson = new Gson();
        listaMedicamentos = new ArrayList<>();

        cargarMedicamentos();

        //aquí usé un callback para simplificar la eliminación de medicamentos
        // y evitar el uso de una interfaz adicional
        adapter = new MedicamentosAdapter(listaMedicamentos, this::confirmarEliminacion); // Pasamos función de eliminar
        binding.recyclerViewMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMedicamentos.setAdapter(adapter);

        binding.fabAgregarMedicamento.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrarMedicamentoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listaMedicamentos.clear();
        cargarMedicamentos();
        adapter.notifyDataSetChanged();
    }

    private void cargarMedicamentos() {
        int total = preferences.getInt("totalMedicamentos", 0);
        listaMedicamentos.clear();

        if (total == 0) {
            // Mostrar estado vacío
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            for (int i = 0; i < total; i++) {
                String json = preferences.getString("medicamento_" + i, null);
                if (json != null) {
                    Medicamento m = gson.fromJson(json, Medicamento.class);
                    listaMedicamentos.add(m);
                }
            }
        }
    }


    private void confirmarEliminacion(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de eliminar este medicamento?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarMedicamento(position))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarMedicamento(int position) {
        // Eliminar de lista
        listaMedicamentos.remove(position);

        // Guardar lista actualizada
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putInt("totalMedicamentos", listaMedicamentos.size());
        for (int i = 0; i < listaMedicamentos.size(); i++) {
            String json = gson.toJson(listaMedicamentos.get(i));
            editor.putString("medicamento_" + i, json);
        }
        editor.apply();

        // Actualizar UI
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
    }
}
