package cl.duoc.transaccionesservice.service;


import cl.duoc.transaccionesservice.dto.TransaccionDTO;
import cl.duoc.transaccionesservice.mapper.TransaccionMapper;
import cl.duoc.transaccionesservice.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionService(
            TransaccionRepository transaccionRepository) {

        this.transaccionRepository = transaccionRepository;
    }


    public List<TransaccionDTO> obtenerTodas() {

        return transaccionRepository
                .findAll()
                .stream()
                .map(TransaccionMapper::toDTO)
                .toList();
    }


    public Optional<TransaccionDTO> obtenerPorId(Integer id) {

        return transaccionRepository
                .findById(id)
                .map(TransaccionMapper::toDTO);
    }
}