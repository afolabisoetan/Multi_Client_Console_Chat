import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server extends Thread {

        private ServerSocket ss = null;
        private static  int totalClients = 0;

        public Server(int numClients) {
            totalClients = numClients; // sets the number of clients this server will run for (dynamic, not hardcoded to only 2)
            try {
                ss = new ServerSocket(5000); // creates the server socket for port 5000
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }

            // creates a new thread for each client-server interaction for each client
            for (int i = 0; i < totalClients; i++) {
                Thread t = new Thread(() -> run());
                t.start();
            }





        }


        @Override
        public void run() {
            // creates a server-client interaction for each thread by creating a ServerBroadcast for each thread
            // for each thread, ss.accept() returns a different socket, so it creates independent client-server interactions
            // for each thread
            new ServerBroadcast(ss);
        }

        public static void addClient ()
        {
            totalClients++;
        }

    public static void main(String[] args) {
        Server s = new Server(3);
    // instantiates a server object with its number of clients (in this case 3)
    }
}

    class ServerBroadcast
    {   private  ServerSocket ss = null;
        private  DataInputStream inputFromClient = null;
        private  DataOutputStream outputToClient = null;
        private  static DataInputStream readFromConsole = new DataInputStream(System.in);
        private String message = "";
        private boolean status = true;

        // creates a list of all the output streams to all the clients (must be static)
        private static java.util.List<DataOutputStream> allClients = new java.util.ArrayList<>();



        public ServerBroadcast (ServerSocket serverSocket)
        {
            this.ss = serverSocket;
            try {
                // creates a socket-server connection between the server socket and a client socket that connects to the server
                // socket through port 5000 (ss.accept() does not return the same socket twice so we need not worry
                // about different threads having the same socket-server connection

                // once the socket is received, use it to initialize the input and output streams, and add that stream
                // to the list of all output streams
                Socket s = ss.accept();
                outputToClient = new DataOutputStream(s.getOutputStream());
                allClients.add(outputToClient);
                inputFromClient = new DataInputStream(s.getInputStream());

                // receives username from client (very first message received)
                String username = inputFromClient.readUTF();

                String welcome = (username + " connected.");
                System.out.println(welcome); // prints a message indicating the user connected

                for(DataOutputStream out : allClients) //messages to all clients (everyone in the list)
                {
                    out.writeUTF( username + " connected.");
                }

                outputToClient.flush(); // flush output stream

                // creates separate receive and send threads so that the client and server can receive
                // and send messages concurrently
                Thread receive = new Thread (() -> {
                    // cycle through loop while the status flag is still true
                    while (status) {
                        try{
                            message = inputFromClient.readUTF();
                            System.out.println(message);

                            // when server receives a message, broadcast to other clients (except this current one since the message
                            // has already been printed)
                            for(DataOutputStream out : allClients)
                            {
                                if(out!= outputToClient )
                                {
                                    out.writeUTF(message);
                                }
                            }

                            // break loop (stop waiting for messages) if the server enters over
                            if(message.toLowerCase().trim().equals("over"))  break;

                        } catch (Exception e) {
                            // prints the user who disconnected which would cause this exception
                            System.out.println(username + " disconnected.");

                            // outputs that the user disconnected for all clients
                            for(DataOutputStream out : allClients)
                            {
                                try {
                                    out.writeUTF( username + " disconnected.");

                                } catch (IOException ex) {
                                    System.out.println("Error disonnecting: " + ex);
                                }
                            }

                            try {
                                // closes communication channel to that client since they have disconnected
                                inputFromClient.close();
                                outputToClient.close();
                            } catch (IOException ex) {
                                System.out.println("Error closing port: " + ex);
                            }
                            allClients.remove(outputToClient); // removes output stream to that client from the list
                            if(allClients.isEmpty())
                            {
                                // closes the server (closes the server socket) if all clients have disconnected
                                try {
                                    System.out.println("Shutting server down since all clients have disconnected...");
                                    System.out.println("Press any button to complete shutdown");
                                    status = false;
                                    ss.close();
                                } catch (IOException ex) {
                                    System.out.println("Error closing port: " + ex);
                                }

                            }
                            break;
                        }
                    }

                });

                // independent message sending thread that allows the server to
                // send and receive messages concurrently
                Thread send =  new Thread (() -> {
                    try {
                        while(status)
                        {
                            try {
                                String reply = readFromConsole.readLine();
                                reply = "Server: " + reply; // makes all messages of format "Server: [message]"
                                outputToClient.writeUTF(reply);
                                // outputs server message for all clients (minus the client it already sent the message to in line 164)
                                for(DataOutputStream out : allClients)
                                {
                                    if(out!= outputToClient )
                                    {
                                        out.writeUTF(reply);
                                    }
                                }

                                // if the client's message (minus their name) is over, close connecting to that client
                                if(reply.substring(8).toLowerCase().trim().equals("over"))
                                {
                                    try {
                                        inputFromClient.close();
                                        outputToClient.close();
                                        ss.close();
                                        status = false; // set status to false so that the receive thread also breaks its loop
                                        break; // break loop in the current send thread (allowing it to reach the end of the thread)
                                    } catch (IOException e) {
                                        System.out.println("Warning " + e);
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                System.out.println("Warning: " + e);
                                break;
                            }

                        }

                    } catch (Exception e) {
                        System.out.println("Warning: " + e);
                    }

                });

                // start the threads
                receive.start();
                send.start();
            } catch (IOException e) {
                System.out.println("Warning: " + e);
            }
        }
    }


