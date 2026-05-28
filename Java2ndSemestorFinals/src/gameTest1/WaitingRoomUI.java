package gameTest1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;

public class WaitingRoomUI extends JFrame {
    
    // Wordle-inspired Color Palette
    private final Color COLOR_BG = new Color(0x12, 0x12, 0x13);          
    private final Color COLOR_CARD = new Color(0x1A, 0x1A, 0x1B);        
    private final Color COLOR_BORDER = new Color(0x3A, 0x3A, 0x3C);      
    private final Color COLOR_TEXT_MAIN = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(0x81, 0x83, 0x84);  
    private final Color COLOR_WORDLE_GREEN = new Color(0x53, 0x8D, 0x4E);

    public WaitingRoomUI(String username, Main mainApp) {
        this.setSize(600, 480); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Wordle Lobby - Player Stats");
        this.setLocationRelativeTo(null);
        
        this.getContentPane().setBackground(COLOR_BG);
        this.setLayout(new BorderLayout());

        
        JPanel pnlMain = new JPanel(new BorderLayout(0, 25));
        pnlMain.setBackground(COLOR_BG);
        pnlMain.setBorder(new EmptyBorder(30, 40, 30, 40));

        
        String displayName = username;
        int level = 1, gamesPlayed = 0, gamesWon = 0, wordsGuessed = 0, wordsCorrect = 0, levelPoints = 0;
        
        try {
            UserManager um = new UserManager();
            displayName = um.getDisplayName(username);
            HashMap<String, Integer> stats = um.getUserStats(username);
            um.closeConnection();

            if (stats != null) {
                level = stats.getOrDefault("Level", 1);
                gamesPlayed = stats.getOrDefault("GamesPlayed", 0);
                gamesWon = stats.getOrDefault("GamesWon", 0);
                wordsGuessed = stats.getOrDefault("WordGuessed", 0);
                wordsCorrect = stats.getOrDefault("WordGuessedCorrectly", 0);
                levelPoints = stats.getOrDefault("LevelPoints", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        
        }

        
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        pnlHeader.setBackground(COLOR_BG);

        JLabel lblWelcome = new JLabel("Welcome back, " + displayName, SwingConstants.CENTER);
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblWelcome.setForeground(COLOR_TEXT_MAIN);

        JLabel lblLevel = new JLabel("LEVEL " + level + "  •  " + levelPoints + " PTS", SwingConstants.CENTER);
        lblLevel.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblLevel.setForeground(COLOR_WORDLE_GREEN);

        pnlHeader.add(lblWelcome);
        pnlHeader.add(lblLevel);
        pnlMain.add(pnlHeader, BorderLayout.NORTH);

        
        JPanel pnlStatsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        pnlStatsGrid.setBackground(COLOR_BG);

        
        double winRate = gamesPlayed > 0 ? ((double) gamesWon / gamesPlayed) * 100 : 0.0;

        pnlStatsGrid.add(createStatCard("Games Played", String.valueOf(gamesPlayed)));
        pnlStatsGrid.add(createStatCard("Win Rate", String.format("%.1f%%", winRate)));
        pnlStatsGrid.add(createStatCard("Total Guesses", String.valueOf(wordsGuessed)));
        pnlStatsGrid.add(createStatCard("Correct Words", String.valueOf(wordsCorrect)));

        pnlMain.add(pnlStatsGrid, BorderLayout.CENTER);

        
        JButton btnJoin = new JButton("JOIN MATCH");
        btnJoin.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnJoin.setBackground(COLOR_WORDLE_GREEN);
        btnJoin.setForeground(Color.WHITE);
        btnJoin.setFocusPainted(false);
        btnJoin.setBorder(new EmptyBorder(14, 0, 14, 0)); 
        btnJoin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        

        btnJoin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnJoin.setBackground(COLOR_WORDLE_GREEN.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnJoin.setBackground(COLOR_WORDLE_GREEN);
            }
        });

        btnJoin.addActionListener(e -> {
            mainApp.startMatch(username);
            this.dispose();
        });

        pnlMain.add(btnJoin, BorderLayout.SOUTH);

        this.add(pnlMain, BorderLayout.CENTER);
        this.setVisible(true);
    }


    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 10, 14, 10)
        ));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblValue.setForeground(COLOR_TEXT_MAIN);

        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);

        return card;
    }
}