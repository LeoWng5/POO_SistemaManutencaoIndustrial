package service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Relógio central do sistema.
 * Quando a simulação está ativa, hoje() e agora() devolvem a data simulada
 * (com a hora real do relógio do PC). Caso contrário, usam o relógio real.
 */
public final class RelogioSistema {

    private static LocalDate dataSimulada = null;

    private RelogioSistema() {}

    /** Data "de hoje" para o sistema (real ou simulada). */
    public static LocalDate hoje() {
        return dataSimulada != null ? dataSimulada : LocalDate.now();
    }

    /** Data e hora atuais (data simulada + hora real). */
    public static LocalDateTime agora() {
        LocalDateTime real = LocalDateTime.now();
        if (dataSimulada == null) {
            return real;
        }
        return LocalDateTime.of(dataSimulada, real.toLocalTime());
    }

    /** Define a data simulada. */
    public static void setData(LocalDate data) {
        dataSimulada = data;
    }

    /** Avança (ou retrocede, com valor negativo) a data simulada em N dias. */
    public static void avancarDias(int dias) {
        LocalDate base = hoje();
        dataSimulada = base.plusDays(dias);
    }

    /** Volta a usar o relógio real. */
    public static void resetar() {
        dataSimulada = null;
    }

    /** True se a data atual está sendo simulada. */
    public static boolean estaSimulando() {
        return dataSimulada != null;
    }
}