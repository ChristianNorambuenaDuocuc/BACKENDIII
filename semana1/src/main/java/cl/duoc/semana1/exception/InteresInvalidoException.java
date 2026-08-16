package cl.duoc.semana1.exception;


public class InteresInvalidoException extends RuntimeException {

    public InteresInvalidoException(String mensaje) {
        super(mensaje);
    }

    public InteresInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
