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
@Table(name = "bandeja_efectivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BandejaEfectivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bandeja")
    private Integer idBandeja;

    @Column(name = "Denominacion", nullable = false)
    private Integer denominacion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "Tipo", nullable = false)
    private Byte tipo;
}
