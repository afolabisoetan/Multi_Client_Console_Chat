import java.io.*;
import java.net.*;
public class Client {
    private Socket s = null;
    private  DataInputStream inputFromServer = null;
    private  DataOutputStream outputToServer = null;
    private  BufferedReader readFromConsole = new BufferedReader(new InputStreamReader(System.in));
    private String message = "";
    private String messageEnder = "over";
    private boolean status = true;

    public Client (String ServeripAddress, String ipAddress, int portNum) {
        try
        {
            // creates a new socket bounded to a port and Ip address sent over
            s  = new Socket();
            s.bind(new InetSocketAddress(ipAddress, portNum));

            // connects the socket to the ip address and port of the server
            s.connect(new InetSocketAddress(ServeripAddress, 5000));

            // creates an input and output stream using the sockets
            inputFromServer = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            outputToServer = new DataOutputStream(s.getOutputStream());

            // forces client to enter a username
            System.out.println("Enter your username: ");
            String username = readFromConsole.readLine();


            // could have used  s.getInetAddress(); instead of username but most chats show a user's username instead of their ip address
            // sends username to server (very first message sent)
            outputToServer.writeUTF(username);
            outputToServer.flush(); // flushes to clear output stream


            Thread receive = new Thread (() -> {
                // allows this thread to continuously wait for messages from the server (until the server types over)
                while (!message.toLowerCase().trim().equals("over") && status) {
                    try{
                        message = inputFromServer.readUTF();
                        System.out.println(message);
                    } catch (Exception e) {
                        // triggered when the client types in over, closing all of its streams and causing this exception
                        // since the inputFromServer stream is closed
                        System.out.println("Disconnecting...");
                        break;
                    }
                }

                // close client if the server closes
                try {
                    inputFromServer.close();
                    outputToServer.close();
                    s.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                }
            });

            // creates an independent send thread that allows the client to send a message to the server
            // while it receives messages from the server
            Thread send =  new Thread (() -> {
                try {
                    while(true) // loops through the thread continuously unless the client types over
                    {
                        try {
                            String reply = readFromConsole.readLine();
                            reply = username + ": " + reply;

                            // creates a message of format username: [message]
                            outputToServer.writeUTF(reply);
                            if(reply.substring(username.length()+2).toLowerCase().trim().equals("over"))
                            {
                                try {
                                    // closes all the streams, sets the status to false (so that the client
                                    // stops listening for server messages) and breaks the loop so that
                                    // the thread can end
                                    inputFromServer.close();
                                    outputToServer.close();
                                    s.close();
                                    status = false;
                                    break;
                                } catch (IOException e) {
                                    System.out.println("Error: " + e);
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error: " + e);
                            break;
                        }

                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }

            });

            // starts the threads

            receive.start();
            send.start();
        } catch (IOException e) {
            System.out.println("Error: " +  e);
        }
    }

}