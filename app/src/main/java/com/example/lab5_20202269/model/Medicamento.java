package com.example.lab5_20202269.model;

public class Medicamento {
    private String nombre;
    private String tipo;
    private String dosis;
    private String frecuenciaHoras;
    private String fechaHoraInicio;

    // Constructor
    public Medicamento(String nombre, String tipo, String dosis, String frecuenciaHoras, String fechaHoraInicio) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.dosis = dosis;
        this.frecuenciaHoras = frecuenciaHoras;
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDosis() {
        return dosis;
    }

    public String getFrecuenciaHoras() {
        return frecuenciaHoras;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }
}
