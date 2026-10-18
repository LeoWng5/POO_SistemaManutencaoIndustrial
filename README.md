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