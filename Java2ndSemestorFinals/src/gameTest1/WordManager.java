package gameTest1;

import java.sql.*;

public class WordManager {
	private Connection conn, conn1;
	String[] words = new String[5]; 
	
	public WordManager() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite:WordleAnswerList.db");
        conn1 = DriverManager.getConnection("jdbc:sqlite:WordleValidWordList.db");
    }
	
	public String[] getWords() throws Exception {
		
        String sql = "SELECT DISTINCT Word FROM Words ORDER BY RANDOM() LIMIT 5";
        
        try (Statement stmt = conn.createStatement();
        		ResultSet rs = stmt.executeQuery(sql)) {
            
        		int index = 0;
        		while (rs.next() && index < words.length) {
        			words[index] = rs.getString("Word");
        			index++;
            }
        }   
        return words;
    }
	
	public boolean wordIsValid(String submittedWord) {
		String sql = "SELECT 1 FROM Words WHERE Word = ? LIMIT 1";
		
		try(PreparedStatement ps = conn1.prepareStatement(sql)){
			ps.setString(1, submittedWord.toUpperCase());
			try(ResultSet rs = ps.executeQuery()){
				return rs.next();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public char[] splitWord(String word) {
		char[] letters = new char[5];
		
		for (int i = 0; i < 5; i++) {
			letters[i] = word.charAt(i);
		}
		return letters;
	}
	

}
