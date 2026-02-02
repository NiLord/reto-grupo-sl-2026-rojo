package com.rojo.maquina_expendedora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dinero_aceptado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DineroAceptado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "denominacion")
    private int denominacion;

    @Column(name = "tipo", nullable = false)
    private byte tipo;

}