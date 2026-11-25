# Sistema de Manutenção de Máquinas Industriais

Este projeto foi desenvolvido para a disciplina de Programação Orientada a Objetos para a matéria de POO no curso de Engenharia de Controle e Automação, da Universidade Júlio de Mesquita Filho no campus ICTS Sorocaba. Tratando de um sistema que irá monitorar, acompanha a manutenção, realizar a gestão de técnicos e tipos de manutenção e estoque de peças para devidos consertos.

## Como Executar
1. Abra o terminal na pasta raiz do projeto.
2. Compile o projeto: `javac App.java` (ou use o botão "Run" do VS Code).
3. Execute a aplicação: `java App`.

## Estrutura do Projeto
*   `classes/`: Contém as entidades do mundo real (Máquinas, Técnicos, Peças, etc).
*   `exception/`: Contém as exceções customizadas para regras de negócio.
*   `service/`: Contém a classe `SistemaService`, que centraliza a lógica e as regras.
*   `App.java`: O ponto de entrada do sistema (Interface de Console).
*   `dados.dat`: Arquivo gerado automaticamente para persistência (salvar os dados).

## Funcionalidades
*   Cadastro de Máquinas, Técnicos e Peças.
*   Abertura de manutenções corretivas e preventivas.
*   Atribuição de técnicos (validando se estão ocupados).
*   Controle de estoque (baixa de peças com validação).
*   Cálculo de custo de mão de obra + peças.
*   Persistência de dados em arquivo.

## 📖 Como Usar o Sistema (Manual do Usuário)
O sistema funciona através de um menu interativo no terminal. Para testar o fluxo completo, siga a ordem lógica de operações:

1. **Cadastrar a base:** Use as opções `1`, `2` e `3` para cadastrar uma Máquina, um Técnico e as Peças no estoque.
2. **Registrar o problema:** Use a opção `4` para registrar uma falha em uma máquina. O sistema automaticamente mudará o status da máquina para "QUEBRADA".
3. **Abrir a manutenção:** Use a opção `5` para abrir uma ordem de serviço corretiva para essa máquina.
4. **Alocar o técnico:** Use a opção `6` para atribuir um técnico à manutenção. O sistema verificará se ele está disponível. Se estiver, ele ficará "OCUPADO".
5. **Concluir:** Use a opção `7` com o ID da manutenção. O sistema calculará o custo (mão de obra + peças), mudará a máquina de volta para "ATIVA" e liberará o técnico.
6. **Relatório:** A opção `8` exibe o histórico e os custos de todas as manutenções realizadas.
7. **Sair:** A opção `0` encerra o sistema e salva todos os dados automaticamente.

## 🧠 A Lógica de Navegação do `App.java`
O arquivo `App.java` é o ponto de entrada (a "tela" do sistema). A lógica de navegação foi construída da seguinte forma:

* **Loop Infinito Controlado:** Um laço `while (op != 0)` mantém o menu rodando na tela até que o usuário digite a opção de sair.
* **Scanner:** A classe `Scanner` captura a entrada do usuário (IDs, nomes, valores) e direciona as informações para o `SistemaService`.
* **Try/Catch Centralizado:** Toda a lógica do menu está envolvida em um bloco `try/catch`. Isso permite que, se o usuário digitar uma letra em vez de número, ou tentar alocar um técnico ocupado, o sistema exiba uma mensagem de erro amigável em vermelho (sem travar ou fechar o programa).
* **Interface Limpa:** O menu utiliza códigos ANSI para limpar a tela a cada operação e adicionar cores (Azul para cadastros, Amarelo para manutenções, Verde para sucesso), melhorando a legibilidade do terminal.
* **Feedback Visual:** Após cada ação, o sistema pausa e pede para o usuário pressionar "ENTER" antes de voltar ao menu principal, dando tempo para ele ler o resultado da operação.