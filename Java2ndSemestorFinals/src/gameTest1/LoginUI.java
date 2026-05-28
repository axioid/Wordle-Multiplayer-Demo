package gameTest1;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginUI extends JFrame {
	
	JPanel p1, p2, p3, p4;
	JTextField tf1, tf2, tf3;
	JButton btnMain, btnToggle;
	JLabel l1, l2, l3;
	UserManager um;
	Main m;
	
	boolean isLoginMode = true;
	
	// --- CHANGED: The constructor now accepts the Main class instance ---
	public LoginUI(Main mainApp) {
		this.m = mainApp; // Store the Main instance
		
		this.setSize(390, 320);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("MCW Login Page");
		this.setLocationRelativeTo(null);
		this.setLayout(new GridLayout(4, 1, 5, 5));
		this.setResizable(false);
		
		try {
			um = new UserManager();
			// REMOVED: m = new Main(); We don't want a new Main, we use the one passed in!
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		
		p1 = new JPanel(new GridBagLayout());
		p2 = new JPanel(new GridBagLayout());
		p3 = new JPanel(new GridBagLayout());
		p4 = new JPanel(new GridBagLayout());
		
		tf1 = new JTextField();		
		tf2 = new JTextField(); 
		tf3 = new JTextField();
		
		btnMain = new JButton("Log in");
		btnToggle = new JButton("Sign up");
		btnMain.setFocusable(false);
		btnToggle.setFocusable(false);
		
		l1 = new JLabel("username:");
		l2 = new JLabel("password:");
		l3 = new JLabel("display name:");

		Dimension labelSize = new Dimension(100, 20);
		l1.setPreferredSize(labelSize);
		l2.setPreferredSize(labelSize);
		l3.setPreferredSize(labelSize);
		
		l1.setHorizontalAlignment(JLabel.RIGHT);
		l2.setHorizontalAlignment(JLabel.RIGHT);
		l3.setHorizontalAlignment(JLabel.RIGHT);

		tf1.setPreferredSize(new Dimension(250, 40));
		tf2.setPreferredSize(new Dimension(250, 40));
		tf3.setPreferredSize(new Dimension(250, 40));
		
		btnMain.setPreferredSize(new Dimension(150, 40));
		btnToggle.setPreferredSize(new Dimension(120, 25)); 
		
		btnToggle.setFont(new Font("Arial", Font.PLAIN, 11));
		btnToggle.setFocusPainted(false);
		btnToggle.setContentAreaFilled(false); 
		btnToggle.setBorderPainted(false);      
		
		btnToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isLoginMode = !isLoginMode; 
				updateMode();                
			}
		});
		
		btnMain.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        if (isLoginMode) {
		            // --- LOG IN MODE ---
		            String username = tf1.getText();
		            String password = tf2.getText();
		            int isLogin = um.isValidLoginInfo(username, password);
		            
		            if (isLogin == 1) {
		                m.start(username); // Tells the Main class to open the WaitingRoom
		                LoginUI.this.dispose(); 
		                
		            } else if (isLogin == 0) {
		                JOptionPane.showMessageDialog(null, "Wrong password", "title", JOptionPane.ERROR_MESSAGE);
		                tf2.setText("");
		            } else if (isLogin == -1) {
		                JOptionPane.showMessageDialog(null, "Username not found", "title", JOptionPane.ERROR_MESSAGE);
		            }
		            
		        } else {
		            // --- SIGN UP MODE ---
		            String username = tf1.getText().trim();
		            String password = tf2.getText();
		            String displayName = tf3.getText().trim();
		            
		            if (um.isUsernameFree(username)) {

		                um.addUser(username, password, displayName);
		                JOptionPane.showMessageDialog(null, "Added user " + username, "Info", JOptionPane.INFORMATION_MESSAGE);
		                isLoginMode = true; 
		                updateMode();
		                
		            } else {
		                JOptionPane.showMessageDialog(null, "Username not available", "Warning", JOptionPane.ERROR_MESSAGE);
		                tf1.setText("");
		            }
		        }
		    }
		});
		
		// --- Add components to Row 1 (Username) ---
		gbc.gridx = 0; gbc.gridy = 0;
		p1.add(l1, gbc);
		gbc.gridx = 1;
		p1.add(tf1, gbc);
		
		// --- Add components to Row 2 (Password) ---
		gbc.gridx = 0; gbc.gridy = 0;
		p2.add(l2, gbc);
		gbc.gridx = 1;
		p2.add(tf2, gbc);
		
		// --- Add components to Row 3 (Display Name) ---
		gbc.gridx = 0; gbc.gridy = 0;
		p3.add(l3, gbc);
		gbc.gridx = 1;
		p3.add(tf3, gbc);
		
		// --- Add components to Row 4 (Buttons Stacked) ---
		GridBagConstraints btnGbc = new GridBagConstraints();
		btnGbc.gridx = 0; 
		btnGbc.gridy = 0;
		p4.add(btnMain, btnGbc);
		
		btnGbc.gridy = 1; // Put toggle button directly underneath
		p4.add(btnToggle, btnGbc);
		
		// Add panels to the frame
		this.add(p1);
		this.add(p2);
		this.add(p3);
		this.add(p4);
		
		// Initialize the default state (Log in mode)
		updateMode();
		
		this.setVisible(true);
	}
	
	// Helper method to refresh the UI based on the mode
	private void updateMode() {
		if (isLoginMode) {
			tf3.setEnabled(false);
			tf3.setText("");
			btnMain.setText("Log in");
			btnToggle.setText("Sign up instead");
		} else {
			tf3.setEnabled(true);
			btnMain.setText("Sign up");
			btnToggle.setText("Log in instead");
		}
	}
}