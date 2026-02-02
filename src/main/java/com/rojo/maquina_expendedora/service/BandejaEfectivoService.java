package com.rojo.maquina_expendedora.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rojo.maquina_expendedora.model.BandejaEfectivo;
import com.rojo.maquina_expendedora.repository.BandejaEfectivoRepository;

@Service
public class BandejaEfectivoService {
    private final BandejaEfectivoRepository BErepository;

    // Inyecta el repositorio
    public BandejaEfectivoService(BandejaEfectivoRepository BErepository) {
        this.BErepository = BErepository;
    }

    public List<BandejaEfectivo> getAllBE() {
        return BErepository.findAll();
    };
}
