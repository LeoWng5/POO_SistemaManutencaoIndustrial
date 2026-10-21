import classes.*;
import exception.*;
import service.SistemaService;
import java.time.LocalDate;
import java.util.Scanner;

public class App {
    
    public static final String RESET = "\u001B[0m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String CIANO = "\u001B[36m";

    public static void main(String[] args) {
        SistemaService service = new SistemaService();
        service.carregar();
        Scanner sc = new Scanner(System.in);
        int op = -1;

        while (op != 0) {
            System.out.print("\033[H\033[2J"); // Limpa a tela
            System.out.flush();
            System.out.println(CIANO + "======================================================");
            System.out.println("        🏭 SISTEMA DE MANUTENÇÃO INDUSTRIAL 🏭        ");
            System.out.println("======================================================" + RESET);
            System.out.println(AZUL + " 1 " + RESET + " - Cadastrar Máquina");
            System.out.println(AZUL + " 2 " + RESET + " - Cadastrar Técnico");
            System.out.println(AZUL + " 3 " + RESET + " - Cadastrar Peça");
            System.out.println(VERMELHO + " 4 " + RESET + " - Excluir Máquina"); // NOVA OPÇÃO
            System.out.println(AMARELO + " 5 " + RESET + " - Registrar Falha");
            System.out.println(AMARELO + " 6 " + RESET + " - Abrir Manutenção");
            System.out.println(AMARELO + " 7 " + RESET + " - Atribuir Técnico");
            System.out.println(AMARELO + " 8 " + RESET + " - Adicionar Peça à Manutenção");
            System.out.println(VERMELHO + " 9 " + RESET + " - Concluir Manutenção");
            System.out.println(VERDE + " 10" + RESET + " - Relatório de Manutenções");
            System.out.println(CIANO + "--- CONSULTAS ---" + RESET);
            System.out.println(CIANO + " 11" + RESET + " - Listar Máquinas");
            System.out.println(CIANO + " 12" + RESET + " - Listar Técnicos");
            System.out.println(CIANO + " 13" + RESET + " - Listar Peças em Estoque");
            System.out.println(" 0 - Salvar e Sair");
            System.out.println(CIANO + "======================================================" + RESET);
            System.out.print("Digite a opção desejada: ");

            try {
                op = Integer.parseInt(sc.nextLine());
                System.out.println();
                
                switch (op) {
                    case 1:
                        System.out.println(VERDE + "--- CADASTRO DE MÁQUINA ---" + RESET);
                        System.out.print("ID (ex: M1): "); String idM = sc.nextLine();
                        System.out.print("Nome: "); String nomeM = sc.nextLine();
                        System.out.print("Modelo: "); String mod = sc.nextLine();
                        System.out.print("Fabricante: "); String fab = sc.nextLine();
                        
                        System.out.println("\nResumo do Cadastro:");
                        System.out.println("ID: " + idM + " | Nome: " + nomeM + " | Modelo: " + mod + " | Fabricante: " + fab);
                        System.out.print("Confirmar cadastro? (S/N): "); String confM = sc.nextLine();
                        
                        if (confM.equalsIgnoreCase("S")) {
                            service.cadastrarMaquina(new Maquina(idM, nomeM, "ATIVA", mod, fab, LocalDate.now()));
                            System.out.println(VERDE + "✔ Máquina cadastrada com sucesso!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Cadastro cancelado." + RESET);
                        }
                        break;
                    
                    case 2:
                        System.out.println(VERDE + "--- CADASTRO DE TÉCNICO ---" + RESET);
                        System.out.print("ID (ex: T1): "); String idT = sc.nextLine();
                        System.out.print("Nome: "); String nomeT = sc.nextLine();
                        System.out.print("Especialidade: "); String esp = sc.nextLine();
                        System.out.print("Telefone: "); String tel = sc.nextLine();
                        
                        System.out.println("\nResumo do Cadastro:");
                        System.out.println("ID: " + idT + " | Nome: " + nomeT + " | Especialidade: " + esp + " | Tel: " + tel);
                        System.out.print("Confirmar cadastro? (S/N): "); String confT = sc.nextLine();

                        if (confT.equalsIgnoreCase("S")) {
                            service.cadastrarTecnico(new Tecnico(idT, nomeT, esp, tel));
                            System.out.println(VERDE + "✔ Técnico cadastrado com sucesso!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Cadastro cancelado." + RESET);
                        }
                        break;
                        
                    case 3:
                        System.out.println(VERDE + "--- CADASTRO DE PEÇA ---" + RESET);
                        System.out.print("ID (ex: P1): "); String idP = sc.nextLine();
                        System.out.print("Nome: "); String nomeP = sc.nextLine();
                        System.out.print("Qtd em estoque: "); int q = Integer.parseInt(sc.nextLine());
                        System.out.print("Valor Unitário (R$): "); double v = Double.parseDouble(sc.nextLine());
                        
                        System.out.println("\nResumo do Cadastro:");
                        System.out.println("ID: " + idP + " | Nome: " + nomeP + " | Qtd: " + q + " | Valor: R$ " + v);
                        System.out.print("Confirmar cadastro? (S/N): "); String confP = sc.nextLine();

                        if (confP.equalsIgnoreCase("S")) {
                            service.cadastrarPeca(new Peca(idP, nomeP, "Descrição", "Geral", q, 5, v));
                            System.out.println(VERDE + "✔ Peça cadastrada no estoque!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Cadastro cancelado." + RESET);
                        }
                        break;

                    case 4:
                        System.out.println(VERMELHO + "--- EXCLUIR MÁQUINA ---" + RESET);
                        service.listarMaquinas();
                        System.out.print("\nDigite o ID da Máquina que deseja excluir: "); String idExc = sc.nextLine();
                        System.out.print("Tem CERTEZA que deseja excluir a máquina " + idExc + "? (S/N): "); String confExc = sc.nextLine();
                        
                        if (confExc.equalsIgnoreCase("S")) {
                            if (service.excluirMaquina(idExc)) {
                                System.out.println(VERDE + "✔ Máquina excluída com sucesso!" + RESET);
                            } else {
                                System.out.println(VERMELHO + "❌ Máquina não encontrada." + RESET);
                            }
                        } else {
                            System.out.println(VERMELHO + "❌ Exclusão cancelada." + RESET);
                        }
                        break;
                        
                    case 5:
                        System.out.println(AMARELO + "--- REGISTRAR FALHA ---" + RESET);
                        System.out.print("ID da Máquina quebrada: "); String idF = sc.nextLine();
                        System.out.print("O que aconteceu? "); String desc = sc.nextLine();
                        
                        System.out.print("Confirmar registro de falha na máquina " + idF + "? (S/N): "); String confF = sc.nextLine();
                        if(confF.equalsIgnoreCase("S")) {
                            service.registrarFalha(desc, idF);
                            System.out.println(VERMELHO + "⚠ Falha registrada! Máquina paralisada." + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Registro cancelado." + RESET);
                        }
                        break;
                        
                    case 6:
                        System.out.println(AMARELO + "--- ABRIR MANUTENÇÃO CORRETIVA ---" + RESET);
                        System.out.print("ID da Máquina: "); String idMC = sc.nextLine();
                        System.out.print("Descrição do serviço: "); String dMC = sc.nextLine();
                        System.out.print("Causa raiz: "); String causa = sc.nextLine();
                        
                        System.out.print("Confirmar abertura de manutenção? (S/N): "); String confMC = sc.nextLine();
                        if(confMC.equalsIgnoreCase("S")) {
                            service.abrirCorretiva(dMC, idMC, causa, "ALTA");
                            System.out.println(VERDE + "✔ Ordem de serviço aberta!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Operação cancelada." + RESET);
                        }
                        break;
                        
                    case 7:
                        System.out.println(AMARELO + "--- ATRIBUIR TÉCNICO ---" + RESET);
                        System.out.print("ID da Manutenção (ex: M1): "); String idMan = sc.nextLine();
                        System.out.print("ID do Técnico (ex: T1): "); String idTec = sc.nextLine();
                        
                        System.out.print("Confirmar alocação do técnico " + idTec + " na manutenção " + idMan + "? (S/N): "); String confAT = sc.nextLine();
                        if(confAT.equalsIgnoreCase("S")) {
                            service.atribuirTecnico(idMan, idTec);
                            System.out.println(VERDE + "✔ Técnico alocado na manutenção!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Operação cancelada." + RESET);
                        }
                        break;

                    case 8:
                        System.out.println(AMARELO + "--- ADICIONAR PEÇA À MANUTENÇÃO ---" + RESET);
                        System.out.print("ID da Manutenção (ex: M1): "); String idManPeca = sc.nextLine();
                        System.out.print("ID da Peça (ex: P1): "); String idPec = sc.nextLine();
                        System.out.print("Quantidade utilizada: "); int qtdPeca = Integer.parseInt(sc.nextLine());
                        
                        System.out.print("Confirmar uso de " + qtdPeca + " unidade(s) da peça " + idPec + "? (S/N): "); String confPeca = sc.nextLine();
                        if(confPeca.equalsIgnoreCase("S")) {
                            service.adicionarPecaNaManutencao(idManPeca, idPec, qtdPeca);
                            System.out.println(VERDE + "✔ Peça adicionada e estoque atualizado!" + RESET);
                        } else {
                            System.out.println(VERMELHO + "❌ Operação cancelada." + RESET);
                        }
                        break;
                        
                    case 9:
                        System.out.println(VERMELHO + "--- CONCLUIR MANUTENÇÃO ---" + RESET);
                        System.out.print("ID da Manutenção: "); String idConc = sc.nextLine();
                        
                        System.out.print("Confirmar a conclusão da manutenção " + idConc + "? (S/N): "); String confConc = sc.nextLine();
                        if(confConc.equalsIgnoreCase("S")) {
                            service.concluirManutencao(idConc);
                        } else {
                            System.out.println(VERMELHO + "❌ Operação cancelada." + RESET);
                        }
                        break;
                        
                    case 10:
                        System.out.println(CIANO + "--- RELATÓRIO GERAL DE MANUTENÇÕES ---" + RESET);
                        service.listarManutencoes();
                        break;
                    
                    case 11:
                        service.listarMaquinas();
                        break;
                        
                    case 12:
                        service.listarTecnicos();
                        break;
                        
                    case 13:
                        service.listarPecas();
                        break;

                    case 0:
                        service.salvar();
                        System.out.println(VERDE + "💾 Dados salvos. Encerrando sistema..." + RESET);
                        break;
                        
                    default:
                        System.out.println(VERMELHO + "Opção inválida!" + RESET);
                }
            } catch (MaquinaInexistenteException | MaquinaInativaException | TecnicoIndisponivelException | ManutencaoFinalizadaException | EstoqueInsuficienteException e) {
                System.out.println("\n" + VERMELHO + "❌ ERRO DE NEGÓCIO: " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println("\n" + VERMELHO + "❌ ERRO: " + e.getMessage() + RESET);
            }
            
            if (op != 0) {
                System.out.println("\nPressione ENTER para voltar ao menu...");
                sc.nextLine();
            }
        }
        sc.close();
    }
}