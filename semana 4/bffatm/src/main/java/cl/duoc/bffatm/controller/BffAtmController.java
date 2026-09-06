package cl.duoc.bffatm.controller;


import cl.duoc.bffatm.dto.*;
import cl.duoc.bffatm.service.BffAtmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atm")
public class BffAtmController {

    private final BffAtmService bffAtmService;

    public BffAtmController(
            BffAtmService bffAtmService) {

        this.bffAtmService = bffAtmService;
    }


    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<SaldoAtmDTO> consultarSaldo(
            @PathVariable Integer cuentaId) {

        SaldoAtmDTO saldo =
                bffAtmService.consultarSaldo(cuentaId);

        if (saldo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(saldo);
    }


    @GetMapping("/cuentas/{cuentaId}/movimientos")
    public ResponseEntity<List<MovimientoAtmDTO>>
    consultarMovimientos(
            @PathVariable Integer cuentaId) {

        return ResponseEntity.ok(
                bffAtmService.consultarMovimientos(cuentaId)
        );
    }


    @PostMapping("/cuentas/{cuentaId}/retiros")
    public ResponseEntity<RetiroResponseDTO> retirar(
            @PathVariable Integer cuentaId,
            @RequestBody RetiroRequestDTO request) {

        return ResponseEntity.ok(
                bffAtmService.validarRetiro(
                        cuentaId,
                        request.getMonto()
                )
        );
    }
}