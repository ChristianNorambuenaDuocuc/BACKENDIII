package cl.duoc.semana1.exception;


public class CuentaAnualInvalidaException extends RuntimeException {

    public CuentaAnualInvalidaException(String mensaje) {
        super(mensaje);
    }

    public CuentaAnualInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
