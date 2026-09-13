package cl.duoc.bffatm.dto;

import java.math.BigDecimal;

public class RetiroResponseDTO {

    private Integer cuentaId;
    private BigDecimal montoSolicitado;
    private boolean aprobado;
    private String mensaje;

    public RetiroResponseDTO() {
    }

    public RetiroResponseDTO(
            Integer cuentaId,
            BigDecimal montoSolicitado,
            boolean aprobado,
            String mensaje) {

        this.cuentaId = cuentaId;
        this.montoSolicitado = montoSolicitado;
        this.aprobado = aprobado;
        this.mensaje = mensaje;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}