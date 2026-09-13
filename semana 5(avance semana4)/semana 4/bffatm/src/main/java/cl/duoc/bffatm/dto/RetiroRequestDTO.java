package cl.duoc.bffatm.dto;


import java.math.BigDecimal;

public class RetiroRequestDTO {

    private BigDecimal monto;

    public RetiroRequestDTO() {
    }

    public RetiroRequestDTO(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
