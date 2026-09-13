package cl.duoc.cuentasservice.controller;


import cl.duoc.cuentasservice.dto.cuentasDTO;
import cl.duoc.cuentasservice.service.cuentasService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class cuentasController {

    private final cuentasService cuentasService;

    public cuentasController(cuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    @GetMapping
    public ResponseEntity<List<cuentasDTO>> obtenerTodas() {

        return ResponseEntity.ok(
                cuentasService.obtenerTodas()
        );
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<List<cuentasDTO>> obtenerPorCuenta(
            @PathVariable Integer cuentaId) {

        List<cuentasDTO> cuentas =
                cuentasService.obtenerPorcuentasId(cuentaId);

        if (cuentas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cuentas);
    }


}