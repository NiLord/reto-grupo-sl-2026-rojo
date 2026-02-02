package com.rojo.maquina_expendedora.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rojo.maquina_expendedora.model.Producto;
import com.rojo.maquina_expendedora.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    // Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        return repo.findAll();
    }

    // Obtener un producto por su ID
    public Producto obtenerProductoPorId(int id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

}
