package com.rojo.maquina_expendedora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rojo.maquina_expendedora.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

}
