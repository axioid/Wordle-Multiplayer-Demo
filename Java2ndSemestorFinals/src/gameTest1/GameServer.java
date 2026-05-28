package gameTest1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer {
    private static final int PORT = 12345;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    
    private static int p1Score = -1, p1Time = -1;
    private static int p2Score = -1, p2Time = -1;

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                
                if (clients.size() < 2) {
                    ClientHandler client = new ClientHandler(socket);
                    clients.add(client);
                    client.start();
                    System.out.println("Player connected. Total: " + clients.size());
                    

                    if (clients.size() == 2) {
                        System.out.println("Two players connected. Generating words...");
                        try {
                            GameLogics serverGl = new GameLogics();
                            String wordPayload = serverGl.getCurrentMatchWord(0) + "," +
                                                 serverGl.getCurrentMatchWord(1) + "," +
                                                 serverGl.getCurrentMatchWord(2) + "," +
                                                 serverGl.getCurrentMatchWord(3) + "," +
                                                 serverGl.getCurrentMatchWord(4);
                                                 

                            broadcast("START:" + wordPayload); 
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    socket.close();
                }
                
                if (clients.isEmpty()) {
                    System.out.println("All players left. Server shutting down.");
                    break; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static synchronized void relayToOpponent(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static synchronized void handleStats(ClientHandler sender, int score, int time) {
        if (clients.size() < 2) return;

        if (sender == clients.get(0)) {
            p1Score = score; p1Time = time;
        } else {
            p2Score = score; p2Time = time;
        }

        
        if (p1Score != -1 && p2Score != -1) {
            if (p1Score > p2Score) {
                clients.get(0).sendMessage("TIE_WIN");
                clients.get(1).sendMessage("TIE_LOSE");
            } else if (p2Score > p1Score) {
                clients.get(1).sendMessage("TIE_WIN");
                clients.get(0).sendMessage("TIE_LOSE");
            } else {
                
                if (p1Time >= p2Time) {
                    clients.get(0).sendMessage("TIE_WIN");
                    clients.get(1).sendMessage("TIE_LOSE");
                } else {
                    clients.get(1).sendMessage("TIE_WIN");
                    clients.get(0).sendMessage("TIE_LOSE");
                }
            }
            
            p1Score = -1; p1Time = -1;
            p2Score = -1; p2Time = -1;
        }
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Player disconnected. Total: " + clients.size());
        
        
        p1Score = -1; p1Time = -1; p2Score = -1; p2Time = -1;

        if (!clients.isEmpty()) {
            clients.get(0).sendMessage("OPPONENT_LEFT");
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        
        public String playerName = "Waiting..."; 

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                System.err.println("Error initializing client streams: " + e.getMessage());
            }
        }

        public void sendMessage(String msg) {
            if (out != null) out.println(msg);
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("NAME:")) {
                        
                        this.playerName = msg.substring(5);
                        
                        relayToOpponent(this, "OPP_NAME:" + this.playerName);
                        
                        
                        for (ClientHandler c : clients) {
                            if (c != this && !c.playerName.equals("Waiting...")) {
                                this.sendMessage("OPP_NAME:" + c.playerName);
                            }
                        }
                    } 
                    else if (msg.startsWith("GUESS:")) {
                        relayToOpponent(this, msg);
                    } 
                    else if (msg.equals("WIN")) {
                        relayToOpponent(this, "LOSE");
                    } 
                    else if (msg.startsWith("STATS:")) {
                        String[] parts = msg.split(":");
                        int score = Integer.parseInt(parts[1]);
                        int time = Integer.parseInt(parts[2]);
                        handleStats(this, score, time);
                    }
                }
            } catch (Exception e) {
                
            } finally {
                removeClient(this);
                try { socket.close(); } catch (Exception e) {}
            }
        }
    }
}
