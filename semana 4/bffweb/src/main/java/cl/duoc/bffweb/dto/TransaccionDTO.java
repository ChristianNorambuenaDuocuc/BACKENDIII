package cl.duoc.bffweb.dto;


import java.math.BigDecimal;

public class TransaccionDTO {

    private Integer id;
    private String fecha;
    private BigDecimal monto;
    private String tipo;

    public TransaccionDTO() {
    }

    public TransaccionDTO(Integer id,
                          String fecha,
                          BigDecimal monto,
                          String tipo) {

        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
