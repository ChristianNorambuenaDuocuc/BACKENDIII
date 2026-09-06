package cl.duoc.cuentasservice.model;

import java.math.BigDecimal;

public class cuentas {

    private Integer cuentasId;
    private String fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;

    public cuentas() {
    }

    public cuentas(Integer cuentasId,
                  String fecha,
                  String transaccion,
                  BigDecimal monto,
                  String descripcion) {

        this.cuentasId = cuentasId;
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public Integer getcuentasId() {
        return cuentasId;
    }

    public void setcuentasId(Integer cuentasId) {
        this.cuentasId = cuentasId;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}