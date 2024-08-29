package app;

import java.io.*;
import java.net.*;

class Servidor extends Thread {
    private final int port;

    public Servidor(int port) {
        this.port = port;
    }

    public void run() {
        try (ServerSocket servidorCentral = new ServerSocket(port)) {
            while (true) {
                Mensagem msg = receberMensagem(servidorCentral);
                if (msg != null) {
                    distribuirMensagem(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Mensagem receberMensagem(ServerSocket servidorCentral) {
        try (Socket conexao = servidorCentral.accept();
             ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
            return (Mensagem) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void distribuirMensagem(Mensagem msg) {
        for (int i = 0; i < 3; i++) {
            try (Socket socketFilial = new Socket("localhost", 5000 + i);
                 ObjectOutputStream saida = new ObjectOutputStream(socketFilial.getOutputStream())) {
                saida.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}