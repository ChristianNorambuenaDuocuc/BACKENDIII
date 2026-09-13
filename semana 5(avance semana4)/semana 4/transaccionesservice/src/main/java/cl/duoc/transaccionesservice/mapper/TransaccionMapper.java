package cl.duoc.transaccionesservice.mapper;


import cl.duoc.transaccionesservice.dto.TransaccionDTO;
import cl.duoc.transaccionesservice.model.Transaccion;

public class TransaccionMapper {

    private TransaccionMapper() {
    }

    public static TransaccionDTO toDTO(Transaccion transaccion) {

        return new TransaccionDTO(
                transaccion.getId(),
                transaccion.getFecha(),
                transaccion.getMonto(),
                transaccion.getTipo()
        );
    }

    public static Transaccion toModel(TransaccionDTO dto) {

        return new Transaccion(
                dto.getId(),
                dto.getFecha(),
                dto.getMonto(),
                dto.getTipo()
        );
    }
}