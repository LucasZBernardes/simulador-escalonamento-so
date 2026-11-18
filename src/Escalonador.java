import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Escalonador {

    static final int QUANTUM = 3;
    static final int MAX_PROCESSOS = 5;
    static final long SEED = 0413;

    static final int TEMPO_DISCO = 4;
    static final int TEMPO_FITA = 6;
    static final int TEMPO_IMPRESSORA = 8;

    static int[] pid = new int[MAX_PROCESSOS];
    static int[] ppid = new int[MAX_PROCESSOS];
    static String[] status = new String[MAX_PROCESSOS];
    static String[] prioridade = new String[MAX_PROCESSOS];
    static int[] tempoChegada = new int[MAX_PROCESSOS];
    static int[] tempoCpuTotal = new int[MAX_PROCESSOS];
    static int[] tempoRestante = new int[MAX_PROCESSOS];
    static String[] proximoIo = new String[MAX_PROCESSOS];
    static int[] momentoIo = new int[MAX_PROCESSOS];
    static int[] tempoIoTotal = new int[MAX_PROCESSOS];
    static int[] tempoIoRestante = new int[MAX_PROCESSOS];
    static int[] tempoFinalizacao = new int[MAX_PROCESSOS];

    static Queue<Integer> filaAlta = new ArrayDeque<>();
    static Queue<Integer> filaBaixa = new ArrayDeque<>();
    static List<Integer> filaIo = new ArrayList<>();

    static int totalPreempcoes = 0;
    static int ioDisco = 0;
    static int ioFita = 0;
    static int ioImpressora = 0;
    static int tempoOcioso = 0;

    public static void inicializarProcessos() {
        Random random = new Random(SEED);
        String[] dispositivos = {"DISCO", "FITA", "IMPRESSORA", "NENHUM"};

        for (int i = 0; i < MAX_PROCESSOS; i++) {
            int cpu = random.nextInt(7) + 4;
            String ioEscolhido = dispositivos[random.nextInt(dispositivos.length)];

            pid[i] = i + 1;
            ppid[i] = 0;
            status[i] = "NOVO";
            prioridade[i] = "ALTA";
            tempoChegada[i] = i * 2;
            tempoCpuTotal[i] = cpu;
            tempoRestante[i] = cpu;
            proximoIo[i] = ioEscolhido;
            tempoIoTotal[i] = 0;
            tempoFinalizacao[i] = 0;

            if (!ioEscolhido.equals("NENHUM")) {
                momentoIo[i] = cpu / 2;
                if (ioEscolhido.equals("DISCO")) {
                    tempoIoRestante[i] = TEMPO_DISCO;
                } else if (ioEscolhido.equals("FITA")) {
                    tempoIoRestante[i] = TEMPO_FITA;
                } else {
                    tempoIoRestante[i] = TEMPO_IMPRESSORA;
                }
            } else {
                momentoIo[i] = -1;
                tempoIoRestante[i] = 0;
            }
        }
    }

    public static void moverNovosProcessosParaFilaAlta(int tempoAtual) {
        for (int i = 0; i < MAX_PROCESSOS; i++) {
            if (status[i].equals("NOVO") && tempoChegada[i] <= tempoAtual) {
                status[i] = "PRONTO";
                prioridade[i] = "ALTA";
                filaAlta.add(i);
                System.out.printf("[t=%03d] P%d criado -> fila ALTA (CPU: %du | I/O: %s)%n",
                        tempoAtual, pid[i], tempoCpuTotal[i], proximoIo[i]);
            }
        }
    }

    public static void atualizarFilasDeIo(int tempoAtual, int unidadesDecorridas) {
        List<Integer> concluidos = new ArrayList<>();

        for (int idx : filaIo) {
            tempoIoRestante[idx] -= unidadesDecorridas;
            tempoIoTotal[idx] += unidadesDecorridas;

            if (tempoIoRestante[idx] <= 0) {
                concluidos.add(idx);
            }
        }

        for (int idx : concluidos) {
            filaIo.remove(Integer.valueOf(idx));
            status[idx] = "PRONTO";
            String dispositivo = proximoIo[idx];
            proximoIo[idx] = "NENHUM";

            if (dispositivo.equals("DISCO")) {
                prioridade[idx] = "BAIXA";
                filaBaixa.add(idx);
                System.out.printf("[t=%03d] P%d retornou do DISCO -> fila BAIXA%n", tempoAtual, pid[idx]);
            } else {
                prioridade[idx] = "ALTA";
                filaAlta.add(idx);
                System.out.printf("[t=%03d] P%d retornou de %s -> fila ALTA%n", tempoAtual, pid[idx], dispositivo);
            }
        }
    }

    public static boolean existemProcessosNaoFinalizados() {
        for (int i = 0; i < MAX_PROCESSOS; i++) {
            if (!status[i].equals("FINALIZADO")) {
                return true;
            }
        }
        return false;
    }

    public static void executarSimulacao() {
        inicializarProcessos();
        int tempo = 0;

        System.out.printf("[config] quantum=%d | processos=%d | seed=%d%n%n", QUANTUM, MAX_PROCESSOS, SEED);

        while (existemProcessosNaoFinalizados()) {
            moverNovosProcessosParaFilaAlta(tempo);

            int idxProcesso = -1;

            if (!filaAlta.isEmpty()) {
                idxProcesso = filaAlta.poll();
            } else if (!filaBaixa.isEmpty()) {
                idxProcesso = filaBaixa.poll();
            } else {
                tempoOcioso++;
                tempo++;
                atualizarFilasDeIo(tempo, 1);
                continue;
            }

            status[idxProcesso] = "EXECUTANDO";
            int tempoExecutadoNoQuantum = 0;
            boolean solicitouIo = false;

            while (tempoExecutadoNoQuantum < QUANTUM && tempoRestante[idxProcesso] > 0) {
                if (!proximoIo[idxProcesso].equals("NENHUM") && tempoRestante[idxProcesso] == momentoIo[idxProcesso]) {
                    solicitouIo = true;
                    break;
                }

                tempoRestante[idxProcesso]--;
                tempoExecutadoNoQuantum++;
                tempo++;

                atualizarFilasDeIo(tempo, 1);
                moverNovosProcessosParaFilaAlta(tempo);
            }

            if (tempoExecutadoNoQuantum > 0) {
                System.out.printf("[t=%03d] CPU executa P%d por %d unidade(s)%n",
                        (tempo - tempoExecutadoNoQuantum), pid[idxProcesso], tempoExecutadoNoQuantum);
            }

            if (tempoRestante[idxProcesso] == 0) {
                status[idxProcesso] = "FINALIZADO";
                tempoFinalizacao[idxProcesso] = tempo;
                System.out.printf("[t=%03d] P%d FINALIZADO%n", tempo, pid[idxProcesso]);

            } else if (solicitouIo) {
                status[idxProcesso] = "BLOQUEADO";
                String disp = proximoIo[idxProcesso];

                if (disp.equals("DISCO")) ioDisco++;
                else if (disp.equals("FITA")) ioFita++;
                else if (disp.equals("IMPRESSORA")) ioImpressora++;

                filaIo.add(idxProcesso);
                System.out.printf("[t=%03d] P%d solicitou I/O %s por %d unidades%n",
                        tempo, pid[idxProcesso], disp, tempoIoRestante[idxProcesso]);

            } else {
                status[idxProcesso] = "PRONTO";
                prioridade[idxProcesso] = "BAIXA";
                filaBaixa.add(idxProcesso);
                totalPreempcoes++;
                System.out.printf("[t=%03d] P%d sofreu preempção -> fila BAIXA%n", tempo, pid[idxProcesso]);
            }
        }

        imprimirResumoFinal(tempo);
    }

    public static void imprimirResumoFinal(int tempoTotal) {
        System.out.printf("%n[fim] Processos finalizados: %d/%d | tempo total: %d%n",
                MAX_PROCESSOS, MAX_PROCESSOS, tempoTotal);
        System.out.println("\n--- MÉTRICAS DA SIMULAÇÃO ---");

        double somaTurnaround = 0;
        double somaEspera = 0;

        for (int i = 0; i < MAX_PROCESSOS; i++) {
            int turnaround = tempoFinalizacao[i] - tempoChegada[i];
            int espera = turnaround - tempoCpuTotal[i] - tempoIoTotal[i];
            somaTurnaround += turnaround;
            somaEspera += espera;
        }

        System.out.println("Total de preempções: " + totalPreempcoes);
        System.out.printf("Eventos de I/O: DISCO=%d, FITA=%d, IMPRESSORA=%d%n", ioDisco, ioFita, ioImpressora);
        System.out.printf("Tempo médio de retorno (Turnaround): %.2f unidades%n", somaTurnaround / MAX_PROCESSOS);
        System.out.printf("Tempo médio de espera: %.2f unidades%n", somaEspera / MAX_PROCESSOS);
        double percOcioso = tempoTotal > 0 ? ((double) tempoOcioso / tempoTotal) * 100.0 : 0.0;
        System.out.printf("Tempo de CPU ociosa: %d unidades (%.1f%%)%n", tempoOcioso, percOcioso);
    }

    public static void main(String[] args) {
        executarSimulacao();
    }
}
