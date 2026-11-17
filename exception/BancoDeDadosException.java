package exception;

/**
 * Erro de banco de dados (sem acesso, chave estrangeira, valor repetido...).
 * É "unchecked" (RuntimeException) para não obrigar try/catch em todo lugar;
 * a tela (App) captura e mostra a mensagem ao usuário.
 */
public class BancoDeDadosException extends RuntimeException {
    public BancoDeDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
