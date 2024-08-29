package app;

import java.io.*;
import java.net.*;
import java.util.Arrays;

class Mensagem implements Serializable {
    private static final long serialVersionUID = 1L;
    public int[] relogioVetorial;
    public double valor;
    public String depositoOuJuro;

    public Mensagem(int[] relogioVetorial, double valor, String depositoOuJuro) {
        this.relogioVetorial = relogioVetorial.clone();
        this.valor = valor;
        this.depositoOuJuro = depositoOuJuro;
    }
}

public class Agencia extends Thread {
    private final int id;
    private double saldoAtual;
    private final int[] vetorRelogio;
    private final InetAddress enderecoServidor;
    private final int port;

    public Agencia(int id, double saldoInicial, InetAddress enderecoServidor, int port) {
        this.id = id;
        this.saldoAtual = saldoInicial;
        this.vetorRelogio = new int[3];
        this.enderecoServidor = enderecoServidor;
        this.port = port;
    }

    private void transmitirMensagem(Mensagem msg) throws IOException {
        exibirTransmissao(msg);
        try (Socket conexao = new Socket(enderecoServidor, port);
             ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream())) {
            saida.writeObject(msg);
        }
    }

    private void exibirTransmissao(Mensagem msg) {
        System.out.println("Agencia " + id + " transmitindo " + msg.depositoOuJuro + " de valor R$" + msg.valor 
                + " - Vetor Relógio: " + Arrays.toString(vetorRelogio));
    }

    private void ajustarSaldo(Mensagem msg) {
        sincronizarRelogio(msg.relogioVetorial);

        if (msg.depositoOuJuro.equals("deposito")) {
            saldoAtual += msg.valor;
        } else if (msg.depositoOuJuro.equals("juros")) {
            aplicarJuros(msg.valor);
        }

        vetorRelogio[id]++;
    }

    private void sincronizarRelogio(int[] relogioRecebido) {
        for (int i = 0; i < vetorRelogio.length; i++) {
            vetorRelogio[i] = Math.max(vetorRelogio[i], relogioRecebido[i]);
        }
    }

    private void aplicarJuros(double taxaJuros) {
        saldoAtual += saldoAtual * (taxaJuros / 100.0);
    }


    private void processarMensagens() {
        
        try (ServerSocket servidorFilial = new ServerSocket(5000 + id)) {
            
            while (true) {
                try (Socket conexao = servidorFilial.accept(); ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
                    Mensagem msg = (Mensagem) entrada.readObject();
                    ajustarSaldo(msg);
                    System.out.println("Agencia " + id + " - Saldo: R$" + saldoAtual + " || Vetor: "
                        + Arrays.toString(vetorRelogio));
                    
                } catch (ClassNotFoundException e) {
                e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        new Thread(this::processarMensagens).start();

        try {
            Thread.sleep(2000);

            if (id == 1) {
                vetorRelogio[id]++;
                double valorDeposito = 100.0;
                Mensagem msg = new Mensagem(vetorRelogio, valorDeposito, "deposito");
                transmitirMensagem(msg);
            }

            if (id == 2) {
                vetorRelogio[id]++;
                double valorJuros = 25.0;
                Mensagem msg = new Mensagem(vetorRelogio, valorJuros, "juros");
                transmitirMensagem(msg);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}