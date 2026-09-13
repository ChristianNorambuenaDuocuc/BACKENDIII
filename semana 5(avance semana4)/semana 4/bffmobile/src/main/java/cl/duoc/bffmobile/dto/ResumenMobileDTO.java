package cl.duoc.bffmobile.dto;


import java.math.BigDecimal;
import java.util.List;

public class ResumenMobileDTO {

    private Integer cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private String tipoCuenta;
    private List<MovimientoMobileDTO> movimientos;

    public ResumenMobileDTO() {
    }

    public ResumenMobileDTO(
            Integer cuentaId,
            String nombre,
            BigDecimal saldo,
            String tipoCuenta,
            List<MovimientoMobileDTO> movimientos) {

        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.movimientos = movimientos;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public List<MovimientoMobileDTO> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(
            List<MovimientoMobileDTO> movimientos) {

        this.movimientos = movimientos;
    }
}
