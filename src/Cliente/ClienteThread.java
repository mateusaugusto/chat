
package Cliente;

import Servidor.Server;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author mateus
 */


// For every client's connection we call this class
public class ClienteThread extends Thread{
  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final ClienteThread[] threads;
  private int maxClientsCount;

  public ClienteThread(Socket clientSocket, ClienteThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    ClienteThread[] threads = this.threads;
    

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
         os.println("Digite seu nome");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("nome invalido");
        }
      }

       /* Msg de bem vindo e envio de conversas anteriores */
      os.println(":: "+ name  + " :: entrou na festa.");
      os.print(Server.mensagens);
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println(":: "+name +" :: entrou na festa.");
          }
        }
      }
     /* Começa a conversa */
      while (true) {
        String line = is.readLine();
        /* atribui novas conversas a variavel globoal de msg */
        Server.mensagens += name +" diz: "+ line + "\n";
        if (line.startsWith("/quit")) {
          break;
        }
        else {
          /* The message is public, broadcast it to all other clients. */
          synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println( name + " diz: " + line  );
              }
            }
          }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("*** The user " + name
                + " is leaving the chat room !!! ***");
          }
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}
