package cl.duoc.semana2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuentaAnual {

    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;

    // Constructor vacío
    public CuentaAnual() {
    }

    // Constructor con todos los atributos
    public CuentaAnual(Long cuentaId, LocalDate fecha, String transaccion,
                       BigDecimal monto, String descripcion) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    // Getters y Setters

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
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

    @Override
    public String toString() {
        return "CuentaAnual{" +
                "cuentaId=" + cuentaId +
                ", fecha=" + fecha +
                ", transaccion='" + transaccion + '\'' +
                ", monto=" + monto +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
