package app;

import java.net.*;

public class Principal {
    public static void main(String[] args) throws Exception {
        InetAddress enderecoServidor = InetAddress.getByName("localhost");

        Servidor servidor = new Servidor(1234);
        servidor.start();

        double saldoInicial = 1000.0;
        int numeroDeAgencias = 3;
        Agencia[] agencias = new Agencia[numeroDeAgencias];

        // Instanciando e inicializando as agências
        for (int i = 0; i < numeroDeAgencias; i++) {
            agencias[i] = new Agencia(i, saldoInicial, enderecoServidor, 1234);
            agencias[i].start();
        }

        // Esperando a conclusão da thread de cada agência
        for (int i = 0; i < numeroDeAgencias; i++) {
            agencias[i].join();
        }
    }
}