package com.rojo.maquina_expendedora.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rojo.maquina_expendedora.model.BandejaEfectivo;

public interface BandejaEfectivoRepository extends JpaRepository<BandejaEfectivo, Integer> {
    public BandejaEfectivo findByDenominacion(Integer denominacion);

    public BandejaEfectivo findByDenominacionAndTipo(Integer denominacion, Byte tipo);

    List<BandejaEfectivo> findAllByOrderByDenominacionDesc();
}
