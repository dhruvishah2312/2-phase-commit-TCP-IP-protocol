/*
Name: Dhruvi Shah
Login ID: dds0403
MavID: 1001550403

Reference code: https://github.com/aboullaite/Multi-Client-Server-chat-application/
 */
/*
 * References: 
 *  1. https://www.tutorialspoint.com/java/java_filewriter_class.htm
 *  2. https://www.mkyong.com/java/how-to-read-file-in-java-fileinputstream/
 *  3. https://github.com/aboullaite/Multi-Client-Server-chat-application/
 *  
 */

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextField;
import javax.swing.JTextPane;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Server {

	private JFrame frame;
	private static final int MAXCLIENTS = 4;
	private static final clientThread[] threads = new clientThread[MAXCLIENTS];
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket;
	public static JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		try {
			serverSocket = new ServerSocket(80);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (true) {
            try {
                clientSocket = serverSocket.accept();

                int i=0;
                for (i = 0; i < MAXCLIENTS; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == MAXCLIENTS) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
	}

	/**
	 * Create the application.
	 */
	public Server() {
			initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textArea = new JTextArea();
		textArea.setRows(4);
		textArea.setColumns(5);
		textArea.setBounds(0, 0, 432, 253);
		frame.getContentPane().add(textArea);
	}

	public static class clientThread extends Thread {

	    private String clientName = null;
	    private DataInputStream is = null;
	    private PrintStream os = null;
	    private Socket clientSocket = null;
	    private final clientThread[] threads;
	    private int maxClientsCount;
	    private String clientsConnected = "0";

	    public clientThread(Socket clientSocket, clientThread[] threads) {
	        this.clientSocket = clientSocket;
	        this.threads = threads;
	        maxClientsCount = threads.length;
	    }

	    public void run() {
	        int maxClientsCount = this.maxClientsCount;
	        clientThread[] threads = this.threads;

	        try {
	            /*
	             * Create input and output streams for this client.
	             */
	            is = new DataInputStream(clientSocket.getInputStream());
	            os = new PrintStream(clientSocket.getOutputStream());

	            Date today = new Date();
	            String httpResponse = "GET HTTP / 1.1 200 OK\r\n" + today + "\r\n"
	                    + "Connection Host : " + clientSocket.getLocalAddress() + ":" + clientSocket.getLocalPort() + "\r\n"
	                    + "Content-Type: text/html";

	            os.println(httpResponse);

	            String name = is.readLine();
	            
	            

//	            while (true) {
//	                os.println("Enter your name.");
//	                name = is.readLine().trim();
//	                System.out.println("Name:"+ name);
//	                if (name.indexOf('@') == -1) {
//	                    break;
//	                } else {
//	                    os.println("The name should not contain '@' character.");
//	                }
//	            }

	            /* Welcome the new the client. */
//	            os.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");
	            synchronized (this) {
	                for (int i = 0; i < maxClientsCount; i++) {
	                    if (threads[i] != null && threads[i] == this) {
	                        clientName = "@" + name;
	                        clientsConnected = "1";
	                        for (int j = 0; j < maxClientsCount; j++) {
                                if (threads[j] != null && threads[j] != this && threads[j].clientName != null && threads[j].clientName.equals("@Coordinator")) {

                                    threads[j].os.println(clientsConnected);
                                    break;
                                }
                            }
	                        break;
	                    }
	                }
//	                for (int i = 0; i < maxClientsCount; i++) {
//	                    if (threads[i] != null && threads[i] != this) {
//	                        threads[i].os.println("*** A new user " + name + " entered the chat room !!! ***");
//	                    }
//	                }
	            }
	            /* Start the conversation. */
	            while (true) {
	                Date current_time = new Date();
	                String time_to_string = new SimpleDateFormat("k:m").format(current_time);
	                String line = is.readLine();
	                if (line.startsWith("/quit")) {
	                    break;
	                }
	                /* If the message is private sent it to the given client. */
	                if (line.startsWith("@")) {
	                    String[] words = line.split("\\s", 2);
	                    if (words.length > 1 && words[1] != null) {
	                        words[1] = words[1].trim();
	                        if (!words[1].isEmpty()) {
	                            synchronized (this) {
	                                for (int i = 0; i < maxClientsCount; i++) {
	                                    if (threads[i] != null && threads[i] != this
	                                            && threads[i].clientName != null
	                                            && threads[i].clientName.equals(words[0])) {
	                                    	
	                                    	textArea.append("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + line + "\n");
	                                        threads[i].os.println("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + words[1]);
	                                        /*
	                                         * Echo this message to let the client know the private
	                                         * message was sent.
	                                         */
	                                        //this.os.println("<" + name + "> [" + time_to_string + "] " + words[1]);
	                                        break;
	                                    }
	                                }
	                            }
	                        }
	                    }
	                } else {
	                    /* The message is public, broadcast it to all other clients. */
	                    synchronized (this) {
	                    	textArea.append("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + line + "\n");
	                        for (int i = 0; i < maxClientsCount; i++) {
	                            if (threads[i] != null && threads[i].clientName != null) {
	                                threads[i].os.println("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + line + "\n");
	                            }
	                        }
	                    }
	                }
	                
//	                synchronized (this) {
//                        for (int i = 0; i < maxClientsCount; i++) {
//                            if (threads[i] != null && threads[i].clientName != null) {
//                                threads[i].os.println("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + line);
//                                textArea.setText("<" + name + "> [" + time_to_string + "] " + httpResponse + "\n" + line);
//                            }
//                        }
//                    }
	            }
	            /* when a client leaves the chat room */
	            
	            synchronized (this) {
	                for (int i = 0; i < maxClientsCount; i++) {
	                    if (threads[i] != null && threads[i] != this
	                            && threads[i].clientName != null) {
	                        threads[i].os.println("*** The user is leaving the chat room !!! ***");
	                    }
	                }
	            }
	            os.println("*** Bye ***");

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

}

