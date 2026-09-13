package cl.duoc.bffmobile.controller;


import cl.duoc.bffmobile.dto.ResumenMobileDTO;
import cl.duoc.bffmobile.service.BffMobileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile")
public class BffMobileController {

    private final BffMobileService bffMobileService;


    public BffMobileController(
            BffMobileService bffMobileService) {

        this.bffMobileService = bffMobileService;
    }


    @GetMapping("/cuentas/{cuentaId}/resumen")
    public ResponseEntity<ResumenMobileDTO> obtenerResumen(
            @PathVariable Integer cuentaId) {

        return ResponseEntity.ok(
                bffMobileService.obtenerResumen(cuentaId)
        );
    }
}