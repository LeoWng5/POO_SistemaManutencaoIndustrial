# Pacote `exception` (Tratamento de Erros)

Contém as exceções customizadas (Checked Exceptions) que estendem `java.lang.Exception`. 
Elas servem para garantir a integridade das regras de negócio do sistema.

*   **MaquinaInexistenteException:** Lançada quando se tenta buscar uma máquina com ID inválido.
*   **MaquinaInativaException:** Lançada ao tentar abrir manutenção em uma máquina desativada.
*   **TecnicoIndisponivelException:** Lançada ao tentar alocar um técnico que já está em outra manutenção.
*   **EstoqueInsuficienteException:** Lançada pela classe `Peca` quando a quantidade solicitada é maior que o estoque.
*   **ManutencaoFinalizadaException:** Lançada ao tentar alterar uma manutenção que já foi concluída.

O uso dessas exceções obriga o `App.java` e o `SistemaService` a tratarem os erros de forma amigável.