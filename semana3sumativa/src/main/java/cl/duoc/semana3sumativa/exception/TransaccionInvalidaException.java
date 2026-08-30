package cl.duoc.semana3sumativa.exception;


public class TransaccionInvalidaException extends RuntimeException {

    public TransaccionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public TransaccionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
