package gameTest1;

public class Main {
    
    public void start(String username) {
        new WaitingRoomUI(username, this);
    }


    public void startMatch(String username) {
        new GameUI(this, username);
    }

    public static void main(String[] args) {
        new LobbyServer().start();
        Main m = new Main();
        new LoginUI(m);
    }
}
