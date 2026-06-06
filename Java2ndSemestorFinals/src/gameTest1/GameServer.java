package gameTest1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer extends Thread {
    private final int port;
    private final LobbyServer lobby;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private volatile ServerSocket serverSocket;
    private volatile boolean running = true;
    private volatile boolean acceptingPlayers = true;
    private volatile boolean matchStarted = false;

    private int p1Score = -1, p1Time = -1;
    private int p2Score = -1, p2Time = -1;

    public GameServer(int port, LobbyServer lobby) {
        this.port = port;
        this.lobby = lobby;
    }

    public int getPort() {
        return port;
    }

    public synchronized int getClientCount() {
        return clients.size();
    }

    public synchronized boolean canAcceptNewPlayer() {
        return acceptingPlayers && clients.size() < 2;
    }

    public synchronized void stopRoom() {
        acceptingPlayers = false;
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            this.serverSocket = socket;
            System.out.println("Room server started on port " + port);

            while (running) {
                Socket clientSocket = socket.accept();
                synchronized (this) {
                    if (clients.size() >= 2) {
                        try (PrintWriter tempOut = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            tempOut.println("BUSY");
                        } catch (Exception ignored) {
                        }
                        try {
                            clientSocket.close();
                        } catch (Exception ignored) {
                        }
                        continue;
                    }

                    ClientHandler client = new ClientHandler(clientSocket);
                    clients.add(client);
                    client.start();
                    System.out.println("Player connected to room " + port + ". Total: " + clients.size());

                    if (clients.size() == 2) {
                        matchStarted = true;
                        acceptingPlayers = false;
                        System.out.println("Two players connected in room " + port + ". Generating words...");
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
                }
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        } finally {
            lobby.removeRoom(this);
            System.out.println("Room on port " + port + " has been removed.");
        }
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void relayToOpponent(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void handleStats(ClientHandler sender, int score, int time) {
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

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Player disconnected from room " + port + ". Total: " + clients.size());

        p1Score = -1; p1Time = -1; p2Score = -1; p2Time = -1;

        if (!clients.isEmpty()) {
            if (matchStarted) {
                acceptingPlayers = false;
            }
            clients.get(0).sendMessage("OPPONENT_LEFT");
        } else {
            stopRoom();
        }
    }

    class ClientHandler extends Thread {
        private final Socket socket;
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
                    } else if (msg.startsWith("GUESS:")) {
                        relayToOpponent(this, msg);
                    } else if (msg.equals("WIN")) {
                        relayToOpponent(this, "LOSE");
                    } else if (msg.startsWith("STATS:")) {
                        String[] parts = msg.split(":");
                        int score = Integer.parseInt(parts[1]);
                        int time = Integer.parseInt(parts[2]);
                        handleStats(this, score, time);
                    }
                }
            } catch (Exception e) {
            } finally {
                removeClient(this);
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
