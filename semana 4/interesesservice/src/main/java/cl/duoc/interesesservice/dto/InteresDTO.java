package cl.duoc.interesesservice.dto;


import java.math.BigDecimal;

public class InteresDTO {

    private Integer interesesId;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;

    public InteresDTO() {
    }

    public InteresDTO(Integer interesesId,
                      String nombre,
                      BigDecimal saldo,
                      Integer edad,
                      String tipo) {

        this.interesesId = interesesId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.edad = edad;
        this.tipo = tipo;
    }

    public Integer getInteresesId() {
        return interesesId;
    }

    public void setInteresesId(Integer interesesId) {
        this.interesesId = interesesId;
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

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}