package cl.duoc.bffatm.dto;


import java.math.BigDecimal;

public class MovimientoAtmDTO {

    private String fecha;
    private String tipo;
    private BigDecimal monto;

    public MovimientoAtmDTO() {
    }

    public MovimientoAtmDTO(
            String fecha,
            String tipo,
            BigDecimal monto) {

        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}