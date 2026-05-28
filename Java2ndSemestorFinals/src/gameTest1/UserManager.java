package gameTest1;

import java.sql.*;
import java.util.HashMap;

public class UserManager {

    String username;
    String displayName;
    Connection conn;
	
    public UserManager() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite:UserDatabse.db");
    }
	
    public boolean addUser(String username, String password, String displayName) {
        String sql = "INSERT INTO Users (Username, Password, DisplayName, WordGuessed, WordGuessedCorrectly, GamesPlayed, GamesWon, LevelPoints, Level) VALUES (?, ?, ?, 0, 0, 0, 0, 0, 1)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, displayName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    // Retrieves all user stats for the Waiting Room
    public HashMap<String, Integer> getUserStats(String username) {
        HashMap<String, Integer> stats = new HashMap<>();
        String sql = "SELECT WordGuessed, WordGuessedCorrectly, GamesPlayed, GamesWon, LevelPoints, Level FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("WordGuessed", rs.getInt("WordGuessed"));
                    stats.put("WordGuessedCorrectly", rs.getInt("WordGuessedCorrectly"));
                    stats.put("GamesPlayed", rs.getInt("GamesPlayed"));
                    stats.put("GamesWon", rs.getInt("GamesWon"));
                    stats.put("LevelPoints", rs.getInt("LevelPoints"));
                    stats.put("Level", rs.getInt("Level"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving stats: " + e.getMessage());
        }
        return stats;
    }

    // Call this at the end of a match to update everything at once
    public void recordMatchResult(String username, int wordsGuessed, int wordsCorrect, boolean wonMatch) {
        HashMap<String, Integer> currentStats = getUserStats(username);
        if (currentStats.isEmpty()) return;

        int newWordGuessed = currentStats.get("WordGuessed") + wordsGuessed;
        int newWordCorrect = currentStats.get("WordGuessedCorrectly") + wordsCorrect;
        int newGamesPlayed = currentStats.get("GamesPlayed") + 1;
        int newGamesWon = currentStats.get("GamesWon") + (wonMatch ? 1 : 0);
        
        // Level Calculation
        int gainedPoints = (wordsCorrect * 10) + (wonMatch ? 50 : 0);
        int newLevelPoints = currentStats.get("LevelPoints") + gainedPoints;
        int newLevel = (int) Math.sqrt(newLevelPoints / 100.0) + 1;

        String sql = "UPDATE Users SET WordGuessed=?, WordGuessedCorrectly=?, GamesPlayed=?, GamesWon=?, LevelPoints=?, Level=? WHERE Username=?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newWordGuessed);
            ps.setInt(2, newWordCorrect);
            ps.setInt(3, newGamesPlayed);
            ps.setInt(4, newGamesWon);
            ps.setInt(5, newLevelPoints);
            ps.setInt(6, newLevel);
            ps.setString(7, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating match stats: " + e.getMessage());
        }
    }
	
	
//	Updates the password for a specific user.
    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

//    Updates the display name for a specific user.
    public boolean updateDisplayName(String username, String newDisplayName) {
        String sql = "UPDATE Users SET DisplayName = ? WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newDisplayName);
            ps.setString(2, username);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating display name: " + e.getMessage());
            return false;
        }
    }


//    Deletes a user entirely from the database.
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isUsernameFree(String username) {
    	String sql = "SELECT * FROM Users WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, username);
    		try(ResultSet rs = ps.executeQuery()){
    			return !rs.next();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    }
    
    public int isValidLoginInfo(String username, String password) {
        String sql = "SELECT Password FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return -1;
                }
                
                String dbPassword = rs.getString("Password");
                if (dbPassword.equals(password)) {
                    this.username = username;
                    this.displayName = getDisplayName(username);
                    return 1;
                } else {
                    return 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            return -1; 
        }
    }
    
//    Closes the database connection.
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }

    
    
    public String getDisplayName(String username) {
        String sql = "SELECT DisplayName FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DisplayName");
                } else {
                    return null; 
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving display name: " + e.getMessage());
            return null;
        }
    }

    public String getUsername() {
        return this.username;
    }
    
    
    //word guessed
    public int getNumberOfWordGuessed(String username) {
        String sql = "SELECT WordGuessed FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nu = rs.getString("WordGuessed");
                    int num;
                    if (nu == null) {
                    	num = 0;
                    } else {
                        num = Integer.parseInt(nu);

                    }
                    return num;
                } else {
                    return 0; 
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving word guessed name: " + e.getMessage());
            return 0;
        }
    }
    
    public void updateWordGuessed(String username) {
    	int wordsGuessed = getNumberOfWordGuessed(username);
    	
    	wordsGuessed++;
    	String stringWordsGuessed = String.valueOf(wordsGuessed);
    	
    	String sql = "UPDATE Users SET WordGuessed = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, stringWordsGuessed);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("update " + username+ " score");
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    
    public void setWordGuessed(String username, int number) {
    	int wordsGuessed = number;
    	wordsGuessed++;
    	String stringWordsGuessed = String.valueOf(wordsGuessed);
    	
    	String sql = "UPDATE Users SET WordGuessed = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, stringWordsGuessed);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("set " + username+ " word guessed: " + number);
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    //words guessed correct
    public int getNumberOfWordGuessedCorrectly(String username) {
        String sql = "SELECT WordGuessedCorrectly FROM Users WHERE Username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nu = rs.getString("WordGuessedCorrectly");
                    int num;
                    if (nu == null) {
                    	num = 0;
                    } else {
                        num = Integer.parseInt(nu);

                    }
                    return num;
                } else {
                    return 0; 
                }
            }
            
        } catch (SQLException e) {
            return 0;
        }
    }
    
    public void updateWordGuessedCorrectly(String username) {
    	int wordsGuessedCorrectly = getNumberOfWordGuessedCorrectly(username);
    	
    	wordsGuessedCorrectly++;
    	String stringWordsGuessedCorrectly = String.valueOf(wordsGuessedCorrectly);
    	
    	String sql = "UPDATE Users SET WordGuessedCorrectly = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, stringWordsGuessedCorrectly);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("update " + username+ " score");
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void setWordGuessedCorrectly(String username, int number) {
    	int wordsGuessedCorrectly = number;
    	wordsGuessedCorrectly++;
    	String stringWordsGuessed = String.valueOf(wordsGuessedCorrectly);
    	
    	String sql = "UPDATE Users SET WordGuessedCorrectly = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, stringWordsGuessed);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("set " + username+ " words guessed correctly: " + number);
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    
    //gamesWon
    public int getGamesWon(String username) {
    	String sql = "SELECT GamesWon FROM Users WHERE Username = ?";
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, username);
    		try(ResultSet rs = ps.executeQuery()){
    			if (rs.next()) {
                    String nu = rs.getString("GamesWon");
                    int num;
                    if (nu == null) {
                    	num = 0;
                    } else {
                        num = Integer.parseInt(nu);
                    }
                    return num;
                } else {
                    return 0; 
                }
    		}
    	} catch (Exception e) {
    		return 0;
    	}
    }
    
    public void updateGamesWon(String username) {
    	int gamesWon = getGamesWon(username);
    	
    	gamesWon++;
    	String strGamesWon = String.valueOf(gamesWon);
    	
    	String sql = "UPDATE Users SET GamesWon = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, strGamesWon);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("update " + username+ " score");
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void setGamesWon(String username, int number) {
    	int gamesWon = number;
    	gamesWon++;
    	String strGamesWon = String.valueOf(gamesWon);
    	
    	String sql = "UPDATE Users SET GamesWon = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, strGamesWon);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("set " + username+ " games won: " + number);
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    
    //games played
    public int getGamesPlayed(String username) {
    	String sql = "SELECT GamesPlayed FROM Users WHERE Username = ?";
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, username);
    		try(ResultSet rs = ps.executeQuery()){
    			if (rs.next()) {
                    String nu = rs.getString("GamesPlayed");
                    int num;
                    if (nu == null) {
                    	num = 0;
                    } else {
                        num = Integer.parseInt(nu);
                    }
                    return num;
                } else {
                    return 0; 
                }
    		}
    	} catch (Exception e) {
    		return 0;
    	}
    }
    
    public void updateGamesPlayed(String username) {
    	int gamesPlayed = getGamesPlayed(username);
    	
    	gamesPlayed++;
    	String strGamesPlayed = String.valueOf(gamesPlayed);
    	
    	String sql = "UPDATE Users SET GamesPlayed = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, strGamesPlayed);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("update " + username+ " score");
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void setGamesPlayed(String username, int number) {
    	int gamesPlayed = number;
    	gamesPlayed++;
    	String strGamesPlayed = String.valueOf(gamesPlayed);
    	
    	String sql = "UPDATE Users SET GamesPlayed = ? WHERE Username = ?";
    	
    	try(PreparedStatement ps = conn.prepareStatement(sql)){
    		ps.setString(1, strGamesPlayed);
    		ps.setString(2, username);
  
            ps.executeUpdate(); 
            System.out.print("set " + username+ " games played: " + number);
            
    	} catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
}
