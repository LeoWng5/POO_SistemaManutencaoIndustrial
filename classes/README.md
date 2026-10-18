# Pacote `classes` (Modelo de Domínio)

Este pacote contém as classes que representam os objetos reais do sistema. Aqui aplicamos os pilares da POO.

## Principais Classes
*   **Equipamento (Abstract):** Classe base genérica. Não pode ser instanciada. Define os atributos comuns.
*   **Maquina:** Herda de Equipamento. Representa as máquinas da fábrica.
*   **MaquinaCNC:** Herda de Maquina (exemplo de polimorfismo).
*   **Tecnico:** Representa os funcionários. Contém os métodos `ocupar()` e `liberar()` para controle de disponibilidade.
*   **Peca:** Representa os itens de estoque. Contém o método `baixarEstoque()` que lança exceção se o estoque for insuficiente.
*   **Manutencao (Abstract):** Classe base para ordens de serviço. Define o contrato `calcularCusto()` e `exibirDetalhes()`.
*   **ManutencaoCorretiva e ManutencaoPreventiva:** Herdam de Manutenção e implementam as regras de cálculo de custo específicas de cada tipo.
*   **ItemPeca:** Associação N:N entre Manutenção e Peça (representa o consumo de peças na manutenção).

## Aplicação de POO
*   **Abstração:** Uso de classes abstratas (`Equipamento` e `Manutencao`).
*   **Encapsulamento:** Atributos privados com getters e setters (ou sem, no caso do ItemPeca).
*   **Herança:** Reutilização de código entre as classes pai e filhas.
*   **Polimorfismo:** O método `calcularCusto()` executa diferentes lógicas dependendo de ser Prevenção ou Correção.