package cl.duoc.interesesservice.mapper;

import cl.duoc.interesesservice.dto.InteresDTO;
import cl.duoc.interesesservice.model.Interes;

public class InteresMapper {

    private InteresMapper() {
    }

    public static InteresDTO toDTO(Interes interes) {

        return new InteresDTO(
                interes.getInteresesId(),
                interes.getNombre(),
                interes.getSaldo(),
                interes.getEdad(),
                interes.getTipo()
        );
    }

    public static Interes toModel(InteresDTO dto) {

        return new Interes(
                dto.getInteresesId(),
                dto.getNombre(),
                dto.getSaldo(),
                dto.getEdad(),
                dto.getTipo()
        );
    }
}