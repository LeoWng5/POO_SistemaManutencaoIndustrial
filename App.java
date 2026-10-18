import classes.*;
import exception.*;
import service.SistemaService;
import java.time.LocalDate;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        SistemaService service = new SistemaService();
        service.carregar();
        Scanner sc = new Scanner(System.in);
        int op = -1;

        while (op != 0) {
            System.out.println("\n=== SISTEMA DE MANUTENÇÃO ===");
            System.out.println("1 - Cadastrar Máquina | 2 - Cadastrar Técnico | 3 - Cadastrar Peça");
            System.out.println("4 - Registrar Falha  | 5 - Abrir Manutenção  | 6 - Atribuir Técnico");
            System.out.println("7 - Concluir Manutenção | 8 - Listar Manutenções | 0 - Sair");
            System.out.print("Opção: ");

            try {
                op = Integer.parseInt(sc.nextLine());
                switch (op) {
                    case 1:
                        System.out.print("ID: "); String idM = sc.nextLine();
                        System.out.print("Nome: "); String nomeM = sc.nextLine();
                        System.out.print("Modelo: "); String mod = sc.nextLine();
                        service.cadastrarMaquina(new Maquina(idM, nomeM, "ATIVA", mod, "Genérico", LocalDate.now()));
                        System.out.println("Máquina cadastrada!");
                        break;
                    case 2:
                        System.out.print("ID: "); String idT = sc.nextLine();
                        System.out.print("Nome: "); String nomeT = sc.nextLine();
                        System.out.print("Especialidade: "); String esp = sc.nextLine();
                        System.out.print("Telefone: "); String tel = sc.nextLine();
                        service.cadastrarTecnico(new Tecnico(idT, nomeT, esp, tel));
                        System.out.println("Técnico cadastrado!");
                        break;
                    case 3:
                        System.out.print("ID: "); String idP = sc.nextLine();
                        System.out.print("Nome: "); String nomeP = sc.nextLine();
                        System.out.print("Qtd em estoque: "); int q = Integer.parseInt(sc.nextLine());
                        System.out.print("Valor: "); double v = Double.parseDouble(sc.nextLine());
                        service.cadastrarPeca(new Peca(idP, nomeP, "Descrição", "Geral", q, 5, v));
                        System.out.println("Peça cadastrada!");
                        break;
                    case 4:
                        System.out.print("ID da Máquina: "); String idF = sc.nextLine();
                        System.out.print("Descrição da Falha: "); String desc = sc.nextLine();
                        service.registrarFalha(desc, idF);
                        System.out.println("Falha registrada e máquina parada!");
                        break;
                    case 5:
                        System.out.print("ID da Máquina: "); String idMC = sc.nextLine();
                        System.out.print("Descrição da Manutenção: "); String dMC = sc.nextLine();
                        System.out.print("Causa do problema: "); String causa = sc.nextLine();
                        service.abrirCorretiva(dMC, idMC, causa, "ALTA");
                        System.out.println("Manutenção corretiva aberta!");
                        break;
                    case 6:
                        System.out.print("ID da Manutenção: "); String idMan = sc.nextLine();
                        System.out.print("ID do Técnico: "); String idTec = sc.nextLine();
                        service.atribuirTecnico(idMan, idTec);
                        System.out.println("Técnico alocado na manutenção!");
                        break;
                    case 7:
                        System.out.print("ID da Manutenção: "); String idConc = sc.nextLine();
                        service.concluirManutencao(idConc);
                        System.out.println("Manutenção concluída e custo calculado!");
                        break;
                    case 8:
                        service.listarManutencoes();
                        break;
                    case 0:
                        service.salvar();
                        System.out.println("Dados salvos. Encerrando sistema...");
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (MaquinaInexistenteException | MaquinaInativaException | TecnicoIndisponivelException | ManutencaoFinalizadaException e) {
                System.out.println("ERRO DE NEGÓCIO: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado ou dado digitado errado.");
            }
        }
        sc.close();
    }
}