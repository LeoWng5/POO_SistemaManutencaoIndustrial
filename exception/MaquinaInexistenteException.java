package exception;

public class MaquinaInexistenteException extends Exception {
    public MaquinaInexistenteException(String mensagem) {
        super(mensagem);
    }
}