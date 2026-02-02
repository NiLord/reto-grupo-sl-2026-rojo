package com.rojo.maquina_expendedora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaItem {
    private Integer idProducto;
    private Integer cantidad;
}
