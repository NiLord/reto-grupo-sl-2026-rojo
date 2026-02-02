package com.rojo.maquina_expendedora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rojo.maquina_expendedora.dto.VentaRequest;
import com.rojo.maquina_expendedora.model.Ventas;
import com.rojo.maquina_expendedora.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    @Autowired
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Endpoint para crear una nueva venta.
     * HTTP Method: POST
     * URL: /api/v1/ventas
     * 
     * @param ventaRequest
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> crearVenta(@RequestBody VentaRequest ventaRequest) {
        ventaService.venderProducto(ventaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}