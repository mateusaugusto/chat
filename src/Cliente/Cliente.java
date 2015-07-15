package Cliente;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Cliente That is the teste {
  public static int id ;
    static class Chat extends Observable {

        private Socket cliente;
        private OutputStream outputStream;
       
       
        
        Scanner leitor = new Scanner(System.in);

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }
       /** Cria cliente de conexão */
        public Chat(String server, int port) throws IOException {            
                        
            cliente = new Socket(server, port);
            outputStream = cliente.getOutputStream();

            Thread receivingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(cliente.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            notifyObservers(line);
                        }
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                }
            };
            receivingThread.start();
        }

        private static final String CRLF = "\r\n"; // nova linha

        public void send(String text) {
            try {

                outputStream.write((text + CRLF).getBytes());
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        /* fecha o cliente */
        public void close() {
            try {
                cliente.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    /**
     * Chat cliente teste
     */
    static class ChatFrame extends JFrame implements Observer {

        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private Chat chatAccess;

        public ChatFrame(Chat chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGUI();
        }

        /* interface */
        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            sendButton = new JButton("Envia");
            box.add(inputTextField);
            box.add(sendButton);

            ActionListener sendListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String str = inputTextField.getText();
                    if (str != null && str.trim().length() > 0) {
                        chatAccess.send(str);
                    }
                    inputTextField.selectAll();
                    inputTextField.requestFocus();
                    inputTextField.setText("");
                }
            };
            inputTextField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.append(finalArg.toString());
                    textArea.append("\n");
                }
            });
        }
    }

    public static void main(String[] args) {
        String server = "127.0.0.1";
        int port = 2222;
        Chat access = null;

        try {
            access = new Chat(server, port);
        } catch (IOException ex) {

            ex.printStackTrace();
            System.exit(0);
        }
        JFrame frame = new ChatFrame(access);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

    }
}
