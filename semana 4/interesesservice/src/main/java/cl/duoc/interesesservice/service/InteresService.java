package cl.duoc.interesesservice.service;


import cl.duoc.interesesservice.dto.InteresDTO;
import cl.duoc.interesesservice.mapper.InteresMapper;
import cl.duoc.interesesservice.repository.InteresRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteresService {

    private final InteresRepository interesRepository;

    public InteresService(InteresRepository interesRepository) {
        this.interesRepository = interesRepository;
    }


    public List<InteresDTO> obtenerTodos() {

        return interesRepository
                .findAll()
                .stream()
                .map(InteresMapper::toDTO)
                .toList();
    }


    public List<InteresDTO> obtenerPorCuentaId(Integer cuentaId) {

        return interesRepository
                .findByCuentaId(cuentaId)
                .stream()
                .map(InteresMapper::toDTO)
                .toList();
    }
}