package cl.duoc.bffatm.dto;


import java.math.BigDecimal;

public class SaldoAtmDTO {

    private Integer cuentaId;
    private BigDecimal saldoDisponible;
    private String tipoCuenta;

    public SaldoAtmDTO() {
    }

    public SaldoAtmDTO(
            Integer cuentaId,
            BigDecimal saldoDisponible,
            String tipoCuenta) {

        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
        this.tipoCuenta = tipoCuenta;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }
}
