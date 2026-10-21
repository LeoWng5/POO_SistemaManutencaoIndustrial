import classes.*;
import exception.*;
import service.SistemaService;

import javax.swing.*;
import java.time.LocalDate;

public class App {

    private static SistemaService service = new SistemaService();

    public static void main(String[] args) {
        // Carrega os dados salvos
        service.carregar();

        // Loop principal do sistema
        while (true) {
            String[] opcoesMenu = {
                "1 - Cadastrar Máquina",
                "2 - Cadastrar Técnico",
                "3 - Cadastrar Peça",
                "4 - Excluir Máquina",
                "5 - Registrar Falha",
                "6 - Abrir Manutenção",
                "7 - Atribuir Técnico",
                "8 - Adicionar Peça à Manutenção",
                "9 - Concluir Manutenção",
                "10 - Relatório de Manutenções (Console)",
                "11 - Listar Máquinas (Console)",
                "12 - Listar Técnicos (Console)",
                "13 - Listar Peças (Console)",
                "0 - Salvar e Sair"
            };

            String escolha = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecione uma opção:\n(Nota: As listagens 10 a 13 aparecerão no terminal/console)",
                    "🏭 Sistema de Manutenção Industrial",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcoesMenu,
                    opcoesMenu[0]
            );

            // Se o usuário fechar a janela ou clicar em Cancelar
            if (escolha == null) {
                service.salvar();
                JOptionPane.showMessageDialog(null, "Dados salvos. Encerrando sistema...", "Adeus", JOptionPane.INFORMATION_MESSAGE);
                break;
            }

            try {
                int op = Integer.parseInt(escolha.split(" ")[0]);

                if (op == 0) {
                    service.salvar();
                    JOptionPane.showMessageDialog(null, "💾 Dados salvos com sucesso. Encerrando...", "Saindo", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }

                switch (op) {
                    case 1: cadastrarMaquina(); break;
                    case 2: cadastrarTecnico(); break;
                    case 3: cadastrarPeca(); break;
                    case 4: excluirMaquina(); break;
                    case 5: registrarFalha(); break;
                    case 6: abrirManutencao(); break;
                    case 7: atribuirTecnico(); break;
                    case 8: adicionarPecaManutencao(); break;
                    case 9: concluirManutencao(); break;
                    
                    case 10:
                        service.listarManutencoes();
                        JOptionPane.showMessageDialog(null, "Relatório impresso no console/terminal.\nVerifique a aba de saída do seu VS Code.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 11:
                        service.listarMaquinas();
                        JOptionPane.showMessageDialog(null, "Lista de máquinas impressa no console/terminal.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 12:
                        service.listarTecnicos();
                        JOptionPane.showMessageDialog(null, "Lista de técnicos impressa no console/terminal.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 13:
                        service.listarPecas();
                        JOptionPane.showMessageDialog(null, "Lista de peças impressa no console/terminal.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                        break;
                }
            } catch (Exception e) {
                // Catch genérico para segurança
                JOptionPane.showMessageDialog(null, "❌ ERRO INESPERADO:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- MÉTODOS DE INTERFACE (JOptionPane) ---

    private static void cadastrarMaquina() {
        String id = JOptionPane.showInputDialog("ID da Máquina (ex: M1):");
        if (id == null) return;
        String nome = JOptionPane.showInputDialog("Nome da Máquina:");
        if (nome == null) return;
        String modelo = JOptionPane.showInputDialog("Modelo:");
        if (modelo == null) return;
        String fabricante = JOptionPane.showInputDialog("Fabricante:");
        if (fabricante == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar cadastro da máquina?\nID: " + id + "\nNome: " + nome + "\nModelo: " + modelo,
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.cadastrarMaquina(new Maquina(id, nome, "ATIVA", modelo, fabricante, LocalDate.now()));
            JOptionPane.showMessageDialog(null, "✔ Máquina cadastrada com sucesso!");
        }
    }

    private static void cadastrarTecnico() {
        String id = JOptionPane.showInputDialog("ID do Técnico (ex: T1):");
        if (id == null) return;
        String nome = JOptionPane.showInputDialog("Nome do Técnico:");
        if (nome == null) return;
        String especialidade = JOptionPane.showInputDialog("Especialidade:");
        if (especialidade == null) return;
        String telefone = JOptionPane.showInputDialog("Telefone:");
        if (telefone == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar cadastro do técnico?\nID: " + id + "\nNome: " + nome + "\nEspecialidade: " + especialidade,
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            service.cadastrarTecnico(new Tecnico(id, nome, especialidade, telefone));
            JOptionPane.showMessageDialog(null, "✔ Técnico cadastrado com sucesso!");
        }
    }

    private static void cadastrarPeca() {
        String id = JOptionPane.showInputDialog("ID da Peça (ex: P1):");
        if (id == null) return;
        String nome = JOptionPane.showInputDialog("Nome da Peça:");
        if (nome == null) return;
        String qtdStr = JOptionPane.showInputDialog("Quantidade em estoque:");
        if (qtdStr == null) return;
        String valorStr = JOptionPane.showInputDialog("Valor unitário (R$):");
        if (valorStr == null) return;

        try {
            int qtd = Integer.parseInt(qtdStr);
            double valor = Double.parseDouble(valorStr);

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Confirmar cadastro da peça?\nID: " + id + "\nNome: " + nome + "\nQtd: " + qtd + "\nValor: R$ " + valor,
                    "Confirmação", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                service.cadastrarPeca(new Peca(id, nome, "Descrição", "Geral", qtd, 5, valor));
                JOptionPane.showMessageDialog(null, "✔ Peça cadastrada com sucesso!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "❌ Erro: Quantidade e Valor devem ser números válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void excluirMaquina() {
        String id = JOptionPane.showInputDialog("Digite o ID da Máquina que deseja excluir:");
        if (id == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja excluir a máquina " + id + "?",
                "Atenção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (service.excluirMaquina(id)) {
                JOptionPane.showMessageDialog(null, "✔ Máquina excluída com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Máquina não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void registrarFalha() {
        String idMaquina = JOptionPane.showInputDialog("ID da Máquina quebrada:");
        if (idMaquina == null) return;
        String descricao = JOptionPane.showInputDialog("O que aconteceu?");
        if (descricao == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar registro de falha na máquina " + idMaquina + "?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.registrarFalha(descricao, idMaquina);
                JOptionPane.showMessageDialog(null, "⚠ Falha registrada! Máquina paralisada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            } catch (MaquinaInexistenteException e) {
                JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void abrirManutencao() {
        String idMaquina = JOptionPane.showInputDialog("ID da Máquina:");
        if (idMaquina == null) return;
        String descricao = JOptionPane.showInputDialog("Descrição do serviço:");
        if (descricao == null) return;
        String causa = JOptionPane.showInputDialog("Causa raiz:");
        if (causa == null) return;

        int confirm = JOptionPane.showConfirmDialog(null, "Confirmar abertura de manutenção corretiva?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.abrirCorretiva(descricao, idMaquina, causa, "ALTA");
                JOptionPane.showMessageDialog(null, "✔ Ordem de serviço aberta!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void atribuirTecnico() {
        String idManutencao = JOptionPane.showInputDialog("ID da Manutenção (ex: M1):");
        if (idManutencao == null) return;
        String idTecnico = JOptionPane.showInputDialog("ID do Técnico (ex: T1):");
        if (idTecnico == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar alocação do técnico " + idTecnico + " na manutenção " + idManutencao + "?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.atribuirTecnico(idManutencao, idTecnico);
                JOptionPane.showMessageDialog(null, "✔ Técnico alocado na manutenção!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void adicionarPecaManutencao() {
        String idManutencao = JOptionPane.showInputDialog("ID da Manutenção (ex: M1):");
        if (idManutencao == null) return;
        String idPeca = JOptionPane.showInputDialog("ID da Peça (ex: P1):");
        if (idPeca == null) return;
        String qtdStr = JOptionPane.showInputDialog("Quantidade utilizada:");
        if (qtdStr == null) return;

        try {
            int qtd = Integer.parseInt(qtdStr);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Confirmar uso de " + qtd + " unidade(s) da peça " + idPeca + "?",
                    "Confirmação", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                service.adicionarPecaNaManutencao(idManutencao, idPeca, qtd);
                JOptionPane.showMessageDialog(null, "✔ Peça adicionada e estoque atualizado!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "❌ Erro: A quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void concluirManutencao() {
        String idManutencao = JOptionPane.showInputDialog("ID da Manutenção:");
        if (idManutencao == null) return;

        int confirm = JOptionPane.showConfirmDialog(null,
                "Confirmar a conclusão da manutenção " + idManutencao + "?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.concluirManutencao(idManutencao);
                JOptionPane.showMessageDialog(null, "✔ Manutenção concluída!\nVerifique o custo no console.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "❌ Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}