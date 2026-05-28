package gameTest1;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import javax.swing.*;

public class GameUI extends JFrame {
	
	static class ScoreCircle extends JPanel {
		Color color = Color.GRAY;
		public ScoreCircle() {
			setPreferredSize(new Dimension(20, 20));
			setBackground(new Color(50, 50, 50));
			setOpaque(true);
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillOval(0, 0, 20, 20);
		}
		public void setColor(Color c) {
			this.color = c;
			repaint();
		}
	}
	
	String[] words = new String[5];
	JPanel pClientGame, pOppGame, pRightBar, pLeftBar, pScorePanel, pKeyboard;
	JLabel lblTimer;
	JLabel lblOppName; 
	JLabel[][] clientLabels = new JLabel[6][5];
	int currentRow = 0;
	int currentCol = 0;
	int match = 0;
	
	HashMap<String, JPanel> keyMap = new HashMap<>();
	
	GameLogics gl;
	UserManager um;
	String displayName;	
	
	Main m;
	String username;
	int roundsWon = 0; 
	
	boolean canPlay = false; 
	javax.swing.Timer roundSwingTimer; 
	javax.swing.Timer breakSwingTimer;
	Timer gameClock; 
	int breakSeconds = 5;
	
	ScoreCircle[] scoreCircles = new ScoreCircle[5];
	
	int highestScore = 0;
	int highestTimeRemaining = 0;
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	
	public GameUI(Main mainApp, String username) {
		this.m = mainApp;
		this.username = username;
		
		this.setSize(800,600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Multiplayer Competitive Wordle");
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		try { gl = new GameLogics(); } catch (Exception e) { e.printStackTrace(); }
		
		try {
			um = new UserManager();
			displayName = um.getDisplayName(username);
			if (displayName == null) displayName = "Player";
		} catch (Exception e) {
			displayName = "Player";
		}
		
		// Left Bar setup
		pLeftBar = new JPanel(new GridBagLayout()); 
		pLeftBar.setPreferredSize(new Dimension(500,600));
		pLeftBar.setBackground(new Color(60, 60, 60));
		
		pClientGame = new JPanel();
		pClientGame.setPreferredSize(new Dimension(350, 420)); 
		pClientGame.setBackground(Color.LIGHT_GRAY);
		pClientGame.setLayout(new GridLayout(6,5,6,7));
		pClientGame.setOpaque(false);
		
		for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBackground(Color.GRAY);
                JLabel letter = new JLabel("", SwingConstants.CENTER);
                letter.setFont(new Font("Arial", Font.BOLD, 30));
                letter.setForeground(Color.WHITE);
                clientLabels[r][c] = letter; 
                cell.add(letter);
                pClientGame.add(cell);
            }
        }
		
		GridBagConstraints gbcLeft = new GridBagConstraints();
		gbcLeft.gridx = 0;
		gbcLeft.gridy = 0;
		gbcLeft.insets = new Insets(20, 0, 40, 0); 
		pLeftBar.add(pClientGame, gbcLeft);
		
		gbcLeft.gridy = 1; 
		gbcLeft.insets = new Insets(0, 0, 20, 0);
		pKeyboard = createKeyboardPanel();
		pLeftBar.add(pKeyboard, gbcLeft);
		
		// Right Bar setup
		pRightBar = new JPanel(new GridBagLayout());
		pRightBar.setPreferredSize(new Dimension(250,600));
		pRightBar.setBackground(new Color(50, 50, 50));

		lblTimer = new JLabel("Waiting for opponent...", SwingConstants.CENTER);
		lblTimer.setFont(new Font("Arial", Font.BOLD, 20)); 
		lblTimer.setForeground(Color.WHITE);
		
		GridBagConstraints gbcRight = new GridBagConstraints();
		gbcRight.gridx = 0;
		gbcRight.gridy = 0;
		gbcRight.insets = new Insets(10, 0, 150, 0); 
		pRightBar.add(lblTimer, gbcRight);
		
		JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		scorePanel.setBackground(new Color(50, 50, 50));
		for (int i = 0; i < 5; i++) {
			scoreCircles[i] = new ScoreCircle();
			scorePanel.add(scoreCircles[i]);
		}
		
		gbcRight.gridy = 1;
		gbcRight.insets = new Insets(0, 0, 30, 0);
		pRightBar.add(scorePanel, gbcRight);
		
		pOppGame = new JPanel();
		pOppGame.setLayout(new GridLayout(6,5,2,2));
		pOppGame.setPreferredSize(new Dimension(150,180));
		pOppGame.setBackground(new Color(60, 60, 60));
		for (int i = 0; i < 30; i++) {
            JPanel p = new JPanel();
            p.setBackground(Color.GRAY);
            pOppGame.add(p);
        }
		
		gbcRight.gridy = 2;
		gbcRight.insets = new Insets(0, 0, 20, 0);
		pRightBar.add(pOppGame, gbcRight);
		
		JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		bottomPanel.setBackground(new Color(50, 50, 50));

		int currentLevel = 1;
		try {
		    HashMap<String, Integer> stats = um.getUserStats(username);
		    if (stats != null && stats.containsKey("Level")) {
		        currentLevel = stats.get("Level");
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		JLabel lblDisplayName = new JLabel("Lv." + currentLevel + " " + displayName, SwingConstants.RIGHT);
		lblDisplayName.setFont(new Font("Arial", Font.BOLD, 16));
		lblDisplayName.setForeground(Color.WHITE);

		lblOppName = new JLabel("Opponent: Waiting...", SwingConstants.LEFT);
		lblOppName.setFont(new Font("Arial", Font.ITALIC, 14));
		lblOppName.setForeground(Color.LIGHT_GRAY);

		bottomPanel.add(lblDisplayName);
		bottomPanel.add(lblOppName);
		
		gbcRight.gridy = 3;
		gbcRight.fill = GridBagConstraints.HORIZONTAL; 
		gbcRight.insets = new Insets(50, 0, 10, 0);
		pRightBar.add(bottomPanel, gbcRight);
		
		this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleInput(e); }
        });
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		
		this.add(pLeftBar, BorderLayout.CENTER);
		this.add(pRightBar, BorderLayout.EAST);
		
		this.addWindowStateListener(new java.awt.event.WindowStateListener() {
		    @Override
		    public void windowStateChanged(java.awt.event.WindowEvent e) {
		        if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
		            pKeyboard.setVisible(true); 
		        } else {
		            pKeyboard.setVisible(false); 
		        }
		        pLeftBar.revalidate();
		        pLeftBar.repaint();
		    }
		});
		
		this.setVisible(true);
		
		canPlay = false;
		setBoardGrayedOut(true);
		connectToServer();
	}
	
	private void connectToServer() {
	    new Thread(() -> {
	        try {
	            socket = new Socket("localhost", 12345);
	            out = new PrintWriter(socket.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            
	            out.println("NAME:" + displayName); 
	            
	            String msg;
	            while ((msg = in.readLine()) != null) {
	                if (msg.startsWith("OPP_NAME:")) {
	                    String oppName = msg.substring(9);
	                    lblOppName.setText("Opponent: " + oppName);
	                    lblOppName.setFont(new Font("Arial", Font.BOLD, 14));
	                    lblOppName.setForeground(Color.WHITE);
	                }
	                else if (msg.startsWith("START:")) { 
	                  
	                    try {
	                        um.updateGamesPlayed(username);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }

	                    String wordData = msg.substring(6);
	                    String[] serverWords = wordData.split(",");
	                    gl.setAllMatchWords(serverWords); 
	                    
	                    lblTimer.setFont(new Font("Arial", Font.BOLD, 45)); 
	                    startPreRoundBreak();
	                } 
	                else if (msg.startsWith("GUESS:")) {
	                    String[] parts = msg.split(":");
	                    int oppRow = Integer.parseInt(parts[1]);
	                    String[] colors = parts[2].split(",");
	                    
	                    Color colorGreen = new Color(83, 141, 78);
	                    Color colorYellow = new Color(181, 159, 59);
	                    Color colorDarkGray = new Color(58, 58, 60);
	                    
	                    Component[] oppCells = pOppGame.getComponents();
	                    for (int i = 0; i < 5; i++) {
	                        int val = Integer.parseInt(colors[i]);
	                        int index = (oppRow * 5) + i;
	                        
	                        if (val == 2) oppCells[index].setBackground(colorGreen);
	                        else if (val == 1) oppCells[index].setBackground(colorYellow);
	                        else oppCells[index].setBackground(colorDarkGray);
	                    }
	                } 
	                else if (msg.equals("LOSE")) {
	                    handleRoundEnd(false);
	                } 
	                else if (msg.equals("TIE_WIN")) {
	                    handleRoundEnd(true);
	                } 
	                else if (msg.equals("TIE_LOSE")) {
	                    handleRoundEnd(false);
	                } 
	                else if (msg.equals("OPPONENT_LEFT")) {
	                    showToast("Opponent Disconnected! You Win.", 3000);
	                    endGame(true); 
	                }
	            }
	        } catch (Exception e) {
	            lblTimer.setFont(new Font("Arial", Font.BOLD, 20));
	            lblTimer.setText("Lost Connection to Server");
	            e.printStackTrace();
	        }
	    }).start();
	}

	private void startPreRoundBreak() {
	    canPlay = false; 
	    clearBoard();    
	    setBoardGrayedOut(true); 
	    
	    highestScore = 0;
	    highestTimeRemaining = 0;
	    
	    breakSeconds = 5;
	    lblTimer.setText("Starts in " + breakSeconds);
	    
	    if (breakSwingTimer != null) breakSwingTimer.stop();
	    breakSwingTimer = new javax.swing.Timer(1000, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            breakSeconds--;
	            if (breakSeconds <= 0) {
	                breakSwingTimer.stop();
	                startRound();
	            } else {
	                lblTimer.setText("Starts in " + breakSeconds);
	            }
	        }
	    });
	    breakSwingTimer.start();
	}
	
	private void startRound() {
	    canPlay = true; 
	    setBoardGrayedOut(false); 
	    
	    gameClock = new Timer(5); 
	    lblTimer.setText(gameClock.getCurrTime());
	    
	    if (roundSwingTimer != null) roundSwingTimer.stop();
	    roundSwingTimer = new javax.swing.Timer(1000, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            gameClock.tick();
	            lblTimer.setText(gameClock.getCurrTime());
	            
	            if (gameClock.isFinished()) {
	                roundSwingTimer.stop();
	                canPlay = false;
	                lblTimer.setText("Waiting...");
	                out.println("STATS:" + highestScore + ":" + highestTimeRemaining);
	            }
	        }
	    });
	    roundSwingTimer.start();
	}
	
	private void handleRoundEnd(boolean won) {
	    canPlay = false; 
	    if (roundSwingTimer != null) roundSwingTimer.stop();
	    
	    scoreCircles[match].setColor(won ? Color.GREEN : Color.RED);
	    
	    if (won) {
	        roundsWon++;
	    }
	    
	    int delayBeforeBreak = won ? 2000 : 2500;

	    if (!won) {
	        String correctWord = "ERROR";
	        try {
	             correctWord = gl.getCurrentMatchWord(match).toUpperCase();
	        } catch (Exception ex) { 
	        	ex.printStackTrace(); 
	        }
	        showToast(correctWord, 2500); 
	    }
	    
	    javax.swing.Timer delayTimer = new javax.swing.Timer(delayBeforeBreak, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            match++;
	            if (match >= 5) {
	                endGame(roundsWon >= 3);
	            } else {
	                startPreRoundBreak();
	            }
	        }
	    });
	    delayTimer.setRepeats(false);
	    delayTimer.start();
	}

	private void endGame(boolean isOverallWinner) {
	    canPlay = false;
	    lblTimer.setText("Game Over");
	    
	    if (roundSwingTimer != null) roundSwingTimer.stop();
	    if (breakSwingTimer != null) breakSwingTimer.stop();
	    
	    
	    try {
	        if (isOverallWinner) {
	            um.updateGamesWon(username);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    
	    if (isOverallWinner) {
	        showToast("MATCH COMPLETE! YOU WON! 🏆", 4000);
	    } else {
	        showToast("Match Complete. You lost.", 4000);
	    }

	    javax.swing.Timer exitTimer = new javax.swing.Timer(4000, e -> {
	        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ex) { ex.printStackTrace(); }
	        
	        GameUI.this.dispose();
	        
	        if (m != null) {
	            m.start(username); 
	        }
	    });
	    exitTimer.setRepeats(false);
	    exitTimer.start();
	}

	private void clearBoard() {
	    for (int r = 0; r < 6; r++) {
	        for (int c = 0; c < 5; c++) {
	            clientLabels[r][c].setText("");
	            ((JPanel) clientLabels[r][c].getParent()).setBackground(Color.GRAY);
	        }
	    }
	    for (JPanel keyPanel : keyMap.values()) {
	        keyPanel.setBackground(Color.GRAY);
	    }
	    
	    Component[] oppCells = pOppGame.getComponents();
	    for(Component c : oppCells) {
	        c.setBackground(Color.GRAY);
	    }
	    
	    currentRow = 0;
	    currentCol = 0;
	}
	
	private void setBoardGrayedOut(boolean isGrayedOut) {
	    Color targetColor = isGrayedOut ? new Color(40, 40, 40) : Color.GRAY;
	    for (int r = 0; r < 6; r++) {
	        for (int c = 0; c < 5; c++) {
	            ((JPanel) clientLabels[r][c].getParent()).setBackground(targetColor);
	        }
	    }
	}
	
	private void showToast(String message, int durationMs) {
	    JWindow toast = new JWindow(this);
	    toast.setBackground(new Color(0,0,0,0)); 
	    toast.setFocusableWindowState(false); 
	    
	    JPanel panel = new JPanel();
	    panel.setBackground(new Color(30, 30, 30, 220));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
	    
	    JLabel label = new JLabel(message, SwingConstants.CENTER);
	    label.setForeground(Color.WHITE);
	    label.setFont(new Font("Arial", Font.BOLD, 16));
	    panel.add(label);
	    
	    toast.add(panel);
	    toast.pack();
	    
	    int x = this.getX() + (this.getWidth() / 2) - (toast.getWidth() / 2);
	    int y = this.getY() + 150; 
	    toast.setLocation(x, y);
	    
	    toast.setAlwaysOnTop(true);
	    toast.setVisible(true);
	    
	    javax.swing.Timer hideTimer = new javax.swing.Timer(durationMs, e -> toast.dispose());
	    hideTimer.setRepeats(false);
	    hideTimer.start();
	}
	
	private JPanel createKeyboardPanel() {
	    JPanel pKeyboard = new JPanel(new GridLayout(3, 1, 5, 5));
	    pKeyboard.setOpaque(false);
	    
	    String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
	    String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
	    String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "BACK"};
	    
	    pKeyboard.add(createKeyRow(row1));
	    pKeyboard.add(createKeyRow(row2));
	    pKeyboard.add(createKeyRow(row3));
	    
	    return pKeyboard;
	}

	private JPanel createKeyRow(String[] keys) {
	    JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
	    rowPanel.setOpaque(false);
	    
	    for (String key : keys) {
	        JPanel pKey = new JPanel(new BorderLayout());
	        pKey.setBackground(Color.GRAY);
	        
	        if (key.equals("ENTER") || key.equals("BACK")) {
	            pKey.setPreferredSize(new Dimension(65, 50));
	        } else {
	            pKey.setPreferredSize(new Dimension(40, 50));
	        }
	        
	        JLabel lbl = new JLabel(key, SwingConstants.CENTER);
	        lbl.setFont(new Font("Arial", Font.BOLD, 14));
	        lbl.setForeground(Color.WHITE);
	        pKey.add(lbl);
	        
	        pKey.setFocusable(false); 
	        lbl.setFocusable(false);
	        
	        keyMap.put(key, pKey); 
	        rowPanel.add(pKey);
	    }
	    return rowPanel;
	}
	
	private void handleInput(KeyEvent e) {
	    if (!canPlay) return; 
	    
        char c = e.getKeyChar();
        
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (currentCol > 0) {
                currentCol--;
                clientLabels[currentRow][currentCol].setText("");
            }
        } 
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentCol == 5) {
                submitGuess();
            }
        }
        else if (Character.isLetter(c) && currentCol < 5) {
            clientLabels[currentRow][currentCol].setText(String.valueOf(c).toUpperCase());
            currentCol++;
        }
    }

	private void submitGuess() {
	    StringBuilder guessStr = new StringBuilder();
	    for (int i = 0; i < 5; i++) {
	        guessStr.append(clientLabels[currentRow][i].getText());
	    }
	    String guess = guessStr.toString();

	    if (guess.length() < 5) return;

	    if (!gl.wm.wordIsValid(guess)) {
	        showToast("Invalid word!", 1000); 
	        return;
	    }
	    
	    try {
	        um.updateWordGuessed(username);

	        int[] results = gl.checkWord(guess, match);
	        
	        Color colorGreen = new Color(83, 141, 78);
	        Color colorYellow = new Color(181, 159, 59);
	        Color colorDarkGray = new Color(58, 58, 60);

	        int currentScore = 0;
	        StringBuilder colorPayload = new StringBuilder("GUESS:" + currentRow + ":");

	        for (int i = 0; i < 5; i++) {
	            JLabel label = clientLabels[currentRow][i];
	            JPanel parentCell = (JPanel) label.getParent(); 
	            String letter = label.getText();
	            
	            JPanel keyPanel = keyMap.get(letter);
	            Color currentKeyColor = (keyPanel != null) ? keyPanel.getBackground() : Color.GRAY;

	            currentScore += results[i];
	            colorPayload.append(results[i]).append(i == 4 ? "" : ",");

	            if (results[i] == 2) {
	                parentCell.setBackground(colorGreen);
	                if (keyPanel != null) keyPanel.setBackground(colorGreen);
	            } else if (results[i] == 1) {
	                parentCell.setBackground(colorYellow);
	                if (keyPanel != null && !currentKeyColor.equals(colorGreen)) {
	                    keyPanel.setBackground(colorYellow);
	                }
	            } else {
	                parentCell.setBackground(colorDarkGray);
	                if (keyPanel != null && !currentKeyColor.equals(colorGreen) && !currentKeyColor.equals(colorYellow)) {
	                    keyPanel.setBackground(colorDarkGray);
	                }
	            }
	        }
	        
	        out.println(colorPayload.toString());
	        
	        if (currentScore > highestScore) {
	            highestScore = currentScore;
	            highestTimeRemaining = gameClock.getSecondsRemaining();
	        }
	        
	        boolean won = true;
	        for (int r : results) if (r != 2) won = false;
	        
	        if (won) {
	            um.updateWordGuessedCorrectly(username);
	            out.println("WIN"); 
	            handleRoundEnd(true); 
	        } else {
	            if (currentRow < 5) {
	                currentRow++;
	                currentCol = 0;
	            } else {
	                canPlay = false;
	                lblTimer.setText("Waiting...");
	                out.println("STATS:" + highestScore + ":" + highestTimeRemaining); 
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
