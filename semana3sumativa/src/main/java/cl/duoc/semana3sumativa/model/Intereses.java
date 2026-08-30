package cl.duoc.semana3sumativa.model;


import java.math.BigDecimal;

public class Intereses {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;

    public Intereses() {
    }

    public Intereses(Long cuentaId, String nombre, BigDecimal saldo,
                     Integer edad, String tipo) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.edad = edad;
        this.tipo = tipo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
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

    @Override
    public String toString() {
        return "Intereses{" +
                "cuentaId=" + cuentaId +
                ", nombre='" + nombre + '\'' +
                ", saldo=" + saldo +
                ", edad=" + edad +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}