package com.rojo.maquina_expendedora.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private int id;

    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombre;

    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal costo;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    public Producto() {
    }
}
