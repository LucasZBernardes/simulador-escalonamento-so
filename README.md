**Trabalho Final de Sistemas Operacionais**

Simulador de escalonamento de processos utilizando Round Robin com Feedback e múltiplas filas de prioridade.

**Grupo**
- Aluno 1: [Lucas Zeidler Bernardes]
- Aluno 2: [Gabriel Pereira Garrutti]

**Linguagem utilizada**
Java (sem orientação a objetos complexa).

**Premissas do escalonador**
- Quantum: 3 unidades de tempo.
- Número máximo de processos: 5 processos simultâneos na simulação.
- Tempos de CPU: Gerados "aleatoriamente" (a partir de uma seed) na faixa de 4 a 10 unidades de tempo por processo.
- Tempos de I/O: Duração fixa definida por tipo de dispositivo:
  - Disco: 4 unidades de tempo (retorno para a fila de Baixa Prioridade).
  - Fita Magnética: 6 unidades de tempo (retorno para a fila de Alta Prioridade).
  - Impressora: 8 unidades de tempo (retorno para a fila de Alta Prioridade).
- Critério de geração dos processos: Os processos são criados sequencialmente com intervalos fixos de chegada a cada 2 unidades de tempo (t = 0, 2, 4, 6, 8). Caso o processo possua solicitação de I/O (DISCO, FITA ou IMPRESSORA), o bloqueio ocorre exatamente na metade de seu tempo total de CPU (tempo_cpu / 2).
- Critério de finalização: A simulação encerra quando todos os 5 processos atingem o estado FINALIZADO.
- Semente aleatória: SEED = 0413 (garante que seja exatamente os mesmos tempos de CPU e eventos de I/O em qualquer execução).

**Como executar o projeto**
Compilação e execução normal em Java
javac src/Escalonador.java
java -cp src Escalonador


**O que aparece na saída**
O simulador imprime no console a linha do tempo detalhada de todos os eventos relevantes do escalonamento e um resumo estatístico ao final:
- [config]: Exibe os parâmetros iniciais configurados (quantum, quantidade de processos e seed).
- [t=XXX] P# criado -> fila ALTA: Indica o momento em que um novo processo entra no sistema e é inserido na fila de alta prioridade.
- [t=XXX] CPU executa P# por Y unidade(s): Mostra qual processo ocupou a CPU e por quantas unidades de tempo dentro do quantum.
- [t=XXX] P# sofreu preempção -> fila BAIXA: Ocorre quando o processo consome todo o quantum sem finalizar e sem solicitar I/O, sendo rebaixado para a fila de baixa prioridade.
- [t=XXX] P# solicitou I/O DISPOSITIVO por Y unidades: Indica que o processo foi bloqueado e movido para a fila de I/O.
- [t=XXX] P# retornou de DISPOSITIVO -> fila ALTA/BAIXA: Mostra a conclusão da operação de I/O e a aplicação da regra de feedback para a fila correspondente.
- [t=XXX] P# FINALIZADO: Indica que o tempo restante de CPU do processo chegou a zero.
- [fim] e Métricas: Exibe o total de processos concluídos, tempo total da simulação, número de preempções, contagem de eventos de I/O por dispositivo, tempo médio de retorn(Turnaround), tempo médio de espera e percentual de CPU ociosa.

**Limitações conhecidas**
- Cada processo realiza no máximo uma solicitação de I/O durante todo o seu ciclo de vida (programada para a metade de sua execução de CPU).
- Os três dispositivos de I/O (DISCO, FITA e IMPRESSORA) compartilham uma única lista de espera (filaIo), processando seus tempos de bloqueio de forma paralela (sem fila exclusiva de contenção por dispositivo físico).
- Não foi implementado o empacotamento bônus em container Docker (Dockerfile).