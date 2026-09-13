package cl.duoc.transaccionesservice.controller;

import cl.duoc.transaccionesservice.dto.TransaccionDTO;
import cl.duoc.transaccionesservice.service.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(
            TransaccionService transaccionService) {

        this.transaccionService = transaccionService;
    }


    @GetMapping
    public ResponseEntity<List<TransaccionDTO>> obtenerTodas() {

        return ResponseEntity.ok(
                transaccionService.obtenerTodas()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDTO> obtenerPorId(
            @PathVariable Integer id) {

        return transaccionService
                .obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }
}