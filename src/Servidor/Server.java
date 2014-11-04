package Servidor;

import Cliente.ClienteThread;
import java.io.EOFException;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/**
 *
 * @author mohammed
 */
// the Server class
public class Server {

    // The server socket.
    private static ServerSocket servidor = null;
    // The client socket.
    private static Socket connection = null;

    public static String mensagens = "";

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private static final ClienteThread[] threads = new ClienteThread[maxClientsCount];

    public Server(int porta, int tamFila) {

        try {

            servidor = new ServerSocket(porta, tamFila);

        } catch (EOFException eof) {
            System.out.println("\n Erro de conexão com o cliente\n");
            eof.printStackTrace();

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void runServidor() {

        System.out.println("Servidor pronto e esperando...");
        while (true) {
            try {
                connection = servidor.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClienteThread(connection, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(connection.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    connection.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }

    public static void main(String args[]) {
        Server s = new Server(2222, 0);
        //Server s2 = new Server(2223, 0);
       // Server s3 = new Server(2225, 0);
        s.runServidor();      
       // s2.runServidor();
       // s3.runServidor();
         
        

    }
}
