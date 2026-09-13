package cl.duoc.bffweb.dto;


import java.util.List;

public class ResumenWebDTO {

    private Integer cuentaId;

    private List<CuentaDTO> movimientosCuenta;

    private List<InteresDTO> informacionIntereses;

    private List<TransaccionDTO> transaccionesGenerales;


    public ResumenWebDTO() {
    }


    public ResumenWebDTO(
            Integer cuentaId,
            List<CuentaDTO> movimientosCuenta,
            List<InteresDTO> informacionIntereses,
            List<TransaccionDTO> transaccionesGenerales) {

        this.cuentaId = cuentaId;
        this.movimientosCuenta = movimientosCuenta;
        this.informacionIntereses = informacionIntereses;
        this.transaccionesGenerales = transaccionesGenerales;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public List<CuentaDTO> getMovimientosCuenta() {
        return movimientosCuenta;
    }

    public void setMovimientosCuenta(
            List<CuentaDTO> movimientosCuenta) {

        this.movimientosCuenta = movimientosCuenta;
    }

    public List<InteresDTO> getInformacionIntereses() {
        return informacionIntereses;
    }

    public void setInformacionIntereses(
            List<InteresDTO> informacionIntereses) {

        this.informacionIntereses = informacionIntereses;
    }

    public List<TransaccionDTO> getTransaccionesGenerales() {
        return transaccionesGenerales;
    }

    public void setTransaccionesGenerales(
            List<TransaccionDTO> transaccionesGenerales) {

        this.transaccionesGenerales = transaccionesGenerales;
    }
}
