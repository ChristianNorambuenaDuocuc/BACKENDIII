package cl.duoc.interesesservice.controller;


import cl.duoc.interesesservice.dto.InteresDTO;
import cl.duoc.interesesservice.service.InteresService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intereses")
public class InteresController {

    private final InteresService interesService;

    public InteresController(InteresService interesService) {
        this.interesService = interesService;
    }


    @GetMapping
    public ResponseEntity<List<InteresDTO>> obtenerTodos() {

        return ResponseEntity.ok(
                interesService.obtenerTodos()
        );
    }


    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<InteresDTO>> obtenerPorCuenta(
            @PathVariable Integer cuentaId) {

        List<InteresDTO> intereses =
                interesService.obtenerPorCuentaId(cuentaId);

        if (intereses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(intereses);
    }
}