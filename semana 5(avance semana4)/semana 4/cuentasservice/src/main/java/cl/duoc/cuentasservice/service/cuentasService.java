package cl.duoc.cuentasservice.service;


import cl.duoc.cuentasservice.dto.cuentasDTO;
import cl.duoc.cuentasservice.mapper.cuentasMapper;
import cl.duoc.cuentasservice.repository.cuentasRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class cuentasService {

    private final cuentasRepository cuentasRepository;

    public cuentasService(cuentasRepository cuentasRepository) {
        this.cuentasRepository = cuentasRepository;
    }


    public List<cuentasDTO> obtenerTodas() {

        return cuentasRepository
                .findAll()
                .stream()
                .map(cuentasMapper::toDTO)
                .toList();
    }


    public List<cuentasDTO> obtenerPorcuentasId(Integer cuentasId) {

        return cuentasRepository
                .findBycuentasId(cuentasId)
                .stream()
                .map(cuentasMapper::toDTO)
                .toList();
    }
}