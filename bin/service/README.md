# Pacote `service` (Regras de Negócio)

Este pacote contém a classe `SistemaService`, que atua como o "cérebro" do sistema.
Ela faz a ponte entre a interface (App) e o modelo de dados (classes).

## Responsabilidades do SistemaService
*   **Gerenciar Listas:** Mantém as listas de máquinas, técnicos, peças e manutenções em memória.
*   **Regras de Negócio:** Valida se uma máquina existe, se um técnico está disponível e calcula custos.
*   **Lançamento de Exceções:** Dispara as exceções do pacote `exception` quando uma regra é violada.
*   **Persistência:** Implementa os métodos `salvar()` e `carregar()`, usando `ObjectOutputStream` e `ObjectInputStream` para transformar os objetos em arquivos `.dat` e vice-versa.