package cl.duoc.bffmobile.dto;


import java.math.BigDecimal;

public class MovimientoMobileDTO {

    private String fecha;
    private String transaccion;
    private BigDecimal monto;

    public MovimientoMobileDTO() {
    }

    public MovimientoMobileDTO(
            String fecha,
            String transaccion,
            BigDecimal monto) {

        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}