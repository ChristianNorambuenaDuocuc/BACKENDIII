package cl.duoc.bffweb.controller;


import cl.duoc.bffweb.dto.ResumenWebDTO;
import cl.duoc.bffweb.service.BffWebService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/web")
public class BffWebController {

    private final BffWebService bffWebService;


    public BffWebController(
            BffWebService bffWebService) {

        this.bffWebService = bffWebService;
    }


    @GetMapping("/cuentas/{cuentaId}/resumen")
    public ResponseEntity<ResumenWebDTO> obtenerResumen(
            @PathVariable Integer cuentaId) {

        return ResponseEntity.ok(
                bffWebService.obtenerResumen(cuentaId)
        );
    }
}
