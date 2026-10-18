import classes.*;
import exception.*;
import service.SistemaService;
import java.time.LocalDate;
import java.util.Scanner;

public class App {
    
    // Cores ANSI para o terminal ficar bonito
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
            System.out.print("\033[H\033[2J"); // Limpa a tela no Linux/Mac
            System.out.flush();
            System.out.println(CIANO + "======================================================");
            System.out.println("        🏭 SISTEMA DE MANUTENÇÃO INDUSTRIAL 🏭        ");
            System.out.println("======================================================" + RESET);
            System.out.println(AZUL + " 1 " + RESET + " - Cadastrar Máquina");
            System.out.println(AZUL + " 2 " + RESET + " - Cadastrar Técnico");
            System.out.println(AZUL + " 3 " + RESET + " - Cadastrar Peça");
            System.out.println(AMARELO + " 4 " + RESET + " - Registrar Falha");
            System.out.println(AMARELO + " 5 " + RESET + " - Abrir Manutenção");
            System.out.println(AMARELO + " 6 " + RESET + " - Atribuir Técnico");
            System.out.println(VERMELHO + " 7 " + RESET + " - Concluir Manutenção");
            System.out.println(VERDE + " 8 " + RESET + " - Relatório de Manutenções");
            System.out.println(" 0 - Salvar e Sair");
            System.out.println(CIANO + "======================================================" + RESET);
            System.out.print("Digite a opção desejada: ");

            try {
                op = Integer.parseInt(sc.nextLine());
                System.out.println(); // Pula linha
                
                switch (op) {
                    case 1:
                        System.out.println(VERDE + "--- CADASTRO DE MÁQUINA ---" + RESET);
                        System.out.print("ID (ex: M1): "); String idM = sc.nextLine();
                        System.out.print("Nome: "); String nomeM = sc.nextLine();
                        System.out.print("Modelo: "); String mod = sc.nextLine();
                        service.cadastrarMaquina(new Maquina(idM, nomeM, "ATIVA", mod, "Genérico", LocalDate.now()));
                        System.out.println(VERDE + "✔ Máquina cadastrada com sucesso!" + RESET);
                        break;
                    
                    case 2:
                        System.out.println(VERDE + "--- CADASTRO DE TÉCNICO ---" + RESET);
                        System.out.print("ID (ex: T1): "); String idT = sc.nextLine();
                        System.out.print("Nome: "); String nomeT = sc.nextLine();
                        System.out.print("Especialidade: "); String esp = sc.nextLine();
                        System.out.print("Telefone: "); String tel = sc.nextLine();
                        service.cadastrarTecnico(new Tecnico(idT, nomeT, esp, tel));
                        System.out.println(VERDE + "✔ Técnico cadastrado com sucesso!" + RESET);
                        break;
                        
                    case 3:
                        System.out.println(VERDE + "--- CADASTRO DE PEÇA ---" + RESET);
                        System.out.print("ID (ex: P1): "); String idP = sc.nextLine();
                        System.out.print("Nome: "); String nomeP = sc.nextLine();
                        System.out.print("Qtd em estoque: "); int q = Integer.parseInt(sc.nextLine());
                        System.out.print("Valor Unitário (R$): "); double v = Double.parseDouble(sc.nextLine());
                        service.cadastrarPeca(new Peca(idP, nomeP, "Descrição", "Geral", q, 5, v));
                        System.out.println(VERDE + "✔ Peça cadastrada no estoque!" + RESET);
                        break;
                        
                    case 4:
                        System.out.println(AMARELO + "--- REGISTRAR FALHA ---" + RESET);
                        System.out.print("ID da Máquina quebrada: "); String idF = sc.nextLine();
                        System.out.print("O que aconteceu? "); String desc = sc.nextLine();
                        service.registrarFalha(desc, idF);
                        System.out.println(VERMELHO + "⚠ Falha registrada! Máquina paralisada." + RESET);
                        break;
                        
                    case 5:
                        System.out.println(AMARELO + "--- ABRIR MANUTENÇÃO CORRETIVA ---" + RESET);
                        System.out.print("ID da Máquina: "); String idMC = sc.nextLine();
                        System.out.print("Descrição do serviço: "); String dMC = sc.nextLine();
                        System.out.print("Causa raiz: "); String causa = sc.nextLine();
                        service.abrirCorretiva(dMC, idMC, causa, "ALTA");
                        System.out.println(VERDE + "✔ Ordem de serviço aberta!" + RESET);
                        break;
                        
                    case 6:
                        System.out.println(AMARELO + "--- ATRIBUIR TÉCNICO ---" + RESET);
                        System.out.print("ID da Manutenção (ex: M1): "); String idMan = sc.nextLine();
                        System.out.print("ID do Técnico (ex: T1): "); String idTec = sc.nextLine();
                        service.atribuirTecnico(idMan, idTec);
                        System.out.println(VERDE + "✔ Técnico alocado na manutenção!" + RESET);
                        break;
                        
                    case 7:
                        System.out.println(VERMELHO + "--- CONCLUIR MANUTENÇÃO ---" + RESET);
                        System.out.print("ID da Manutenção: "); String idConc = sc.nextLine();
                        service.concluirManutencao(idConc);
                        System.out.println(VERDE + "✔ Manutenção concluída! Custo calculado." + RESET);
                        break;
                        
                    case 8:
                        System.out.println(CIANO + "--- RELATÓRIO GERAL DE MANUTENÇÕES ---" + RESET);
                        service.listarManutencoes();
                        break;
                        
                    case 0:
                        service.salvar();
                        System.out.println(VERDE + "💾 Dados salvos. Encerrando sistema..." + RESET);
                        break;
                        
                    default:
                        System.out.println(VERMELHO + "Opção inválida!" + RESET);
                }
            } catch (MaquinaInexistenteException | MaquinaInativaException | TecnicoIndisponivelException | ManutencaoFinalizadaException e) {
                System.out.println("\n" + VERMELHO + "❌ ERRO DE NEGÓCIO: " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println("\n" + VERMELHO + "❌ ERRO: Dado inválido digitado." + RESET);
            }
            
            if (op != 0) {
                System.out.println("\nPressione ENTER para voltar ao menu...");
                sc.nextLine();
            }
        }
        sc.close();
    }
}