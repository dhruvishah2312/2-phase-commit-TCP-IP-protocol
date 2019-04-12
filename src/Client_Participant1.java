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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Client_Participant1 {
	private static JTextArea textArea;
    private static JTextField inputTextField;
    private static JButton sendButton;
    private static JButton commitButton;
    private static JButton abortButton;
    public static ArrayList<String> votes = new ArrayList<String>();
    public static String reply = null;
    public static FileReader in = null;
    public static FileOutputStream out = null;
    public static String content = null;
	
	/** Chat client access */
    static class ChatAccess extends Observable {
        private Socket socket;
        private OutputStream outputStream;

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        /** Create socket, and receiving thread */
        public void InitSocket(String server, int port) throws IOException {
            socket = new Socket(server, port);
            outputStream = socket.getOutputStream();
            
            outputStream.write(("Participant_1\n").getBytes());
            outputStream.flush();
            
            // no auto-flushing
            /*PrintWriter pw = new PrintWriter(outputStream, false);
            // native line endings are uncertain so add them manually
            pw.print("GET index HTTP/1.0\r\n");
            pw.print("Accept: text/plain, text/html, text/*\r\n");
            pw.print("\r\n");
            pw.flush();*/

            //Create thread to receive input   
            Thread receivingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null)
                            notifyObservers(line);
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                }
            };
            receivingThread.start();
        }

        private static final String CRLF = "\r\n"; // newline

        /** Send a line of text */
        public void send(String text) {
            try {
                outputStream.write((text + CRLF).getBytes());
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
        
        

        /** Close the socket */
        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    /** Chat client UI */
    static class ChatFrame extends JFrame implements Observer {

    	private static ChatAccess chatAccess;
    	
        public ChatFrame(ChatAccess chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGUI();
        }

        /** Builds the user interface */
        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            
            try {
				in = new FileReader(new File("Participant_1.txt"));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            BufferedReader br = new BufferedReader(in);
            String line;
    		try {
    			if((line = br.readLine()) != null) {
    				textArea.append("File Content:" + line + "\n");
    				while((line = br.readLine()) != null) {
    					textArea.append("File Content: " + line + "\n");					//Store the list in an array
    				}
    			} else {
    				textArea.append("No Contents in file" + "\n");
    			}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            //inputTextField = new JTextField();
            //sendButton = new JButton("Send");
            commitButton = new JButton("Commit");
            abortButton = new JButton("Abort");
            	
            //box.add(inputTextField);
            //box.add(sendButton);
            
            box.add(commitButton);
            box.add(abortButton);
            
            
           
            
            // Action for the inputTextField and the goButton
//            ActionListener sendListener = new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    String str = inputTextField.getText();
//                    if (str != null && str.trim().length() > 0)
//                        chatAccess.send(str);
//                    inputTextField.selectAll();
//                    inputTextField.requestFocus();
//                    inputTextField.setText("");
//                }
//            };
            
            ActionListener commitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	abortButton.setEnabled(false);
                	commitButton.setEnabled(false);
                	
                	Thread commit = new Thread() {
                        @Override
                        public void run() {
                        	phaseCommit("VOTE_COMMIT");
                        }
                    };
                    commit.start();
                }
            };
            
            ActionListener abortListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    abortButton.setEnabled(false);
                    commitButton.setEnabled(false);
                    Thread abort = new Thread() {
                        @Override
                        public void run() {
                            phaseCommit("VOTE_ABORT");
                        }
                    };
                    abort.start();
                }
            };
            
//            inputTextField.addActionListener(sendListener);
//            sendButton.addActionListener(sendListener);
            commitButton.addActionListener(commitListener);
            abortButton.addActionListener(abortListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        /** Updates the UI depending on the Object argument */
        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    
                    if(finalArg.toString().contains("VOTE_REQUEST: COMMIT")) {
                    	textArea.append("START_2PC\nState: INIT");
                    	textArea.append("\n");
                    	textArea.append(finalArg.toString());
                        textArea.append("\n");
                    } else if(finalArg.toString().contains("GLOBAL_COMMIT") || finalArg.toString().contains("GLOBAL_ABORT")) {
                    	reply = finalArg.toString();
                    	textArea.append(finalArg.toString());
                        textArea.append("\n");
                    } else if(finalArg.toString().contains("Store String -")){
                    	String[] words = finalArg.toString().split("-", 2);
                    	content = words[1];
                    	System.out.println(content.trim());
                    	textArea.append(finalArg.toString());
                        textArea.append("\n");
                    } else {
                    	textArea.append(finalArg.toString());
                        textArea.append("\n");
                    }
                }
            });
        }
    
        /*
		 * Initiate 2 Phase Commit
		 */
        public void phaseCommit(String action) {
        	
        	if(action.contains("COMMIT")) {		//Send what action the user wishes to perform
        		chatAccess.send("@Coordinator "+action);
        		textArea.append("VOTE_COMMIT\nState: READY\n");
        	} else if(action.contains("ABORT")) {
        		chatAccess.send("@Coordinator "+action);
        		textArea.append("VOTE_ABORT\nState: ABORT");
            	textArea.append("\n");
        	}
        	
        	long endTimeMillis = System.currentTimeMillis() + 20000;		//Wait for Coordinator Reply
        	while(true) {
        		if((System.currentTimeMillis()-endTimeMillis) > 0) {
        			break;
        		}
        	}
        	
        	System.out.println("Reply:" + reply );
        	if(reply != null) {							//Perform write to file or don't depending on the coordinator decision
        		if(reply.contains("GLOBAL_COMMIT")) {
        			textArea.append("GLOBAL_COMMIT\nState: COMMIT\n");
                	textArea.append("\n");
                	try {
						FileWriter writer = new FileWriter(new File("Participant_1.txt"));
						writer.write(content.trim());
						writer.flush();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	reply = null;
                	chatAccess.send("@Coordinator Commit Complete");
                	
        		} else {
        			textArea.append("GLOBAL_ABORT\nState: ABORT");
                	textArea.append("\n");
                	chatAccess.send("@Coordinator Abort Complete");
        		}
        	}
        	
        	
        	abortButton.setEnabled(true);
            commitButton.setEnabled(true);	
        	
        }
    
        
    }
    
    
  

    public static void main(String[] args) {
        //String server = args[0];
        //int port =2222;
            
//        if(args!=null && !args[1].equals("")){
//            port = Integer.parseInt(args[1]);
//        }else{
//            JOptionPane.showMessageDialog(null, "Plsease enter port number");
//            System.exit(1);
//        }
        ChatAccess access = new ChatAccess();

        JFrame frame = new ChatFrame(access);
        frame.setTitle("MyChatApp - connected to localhost : 80");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        try {
            access.InitSocket("localhost",80);
        } catch (IOException ex) {
            System.out.println("Cannot connect to Localhost : 80 ");
            ex.printStackTrace();
            System.exit(0);
        }
    }


}
