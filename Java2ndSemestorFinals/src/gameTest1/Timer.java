package gameTest1;

public class Timer {
    private int secondsRemaining;

    public Timer(int minutes) {
        this.secondsRemaining = minutes * 60;
    }

    public void tick() {
        if (secondsRemaining > 0) {
            secondsRemaining--;
        }
    }
    public String getCurrTime() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;

        return String.format("%d:%02d", minutes, seconds);
    }
    
    public boolean isFinished() {
        return secondsRemaining <= 0;
    }
    
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}