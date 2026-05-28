package gameTest1;

public class GameLogics {
	WordManager wm;
	String[] sessionWords;
	
	public GameLogics() throws Exception {
		wm = new WordManager();
		this.sessionWords = wm.getWords();
    }
	
	public String getCurrentMatchWord(int matchIndx) throws Exception{
		return sessionWords[matchIndx];	
	}
		
	public int[] checkWord(String guessedWord, int match) throws Exception {
	    char[] guess = guessedWord.toUpperCase().toCharArray();
	    char[] target = getCurrentMatchWord(match).toUpperCase().toCharArray();
	    
	    int[] letterState = new int[5];
	    boolean[] targetUsed = new boolean[5]; 
	    boolean[] guessUsed = new boolean[5];  

	    
	    for (int i = 0; i < 5; i++) {
	        if (guess[i] == target[i]) {
	            letterState[i] = 2;
	            targetUsed[i] = true;
	            guessUsed[i] = true;
	        }
	    }

	    
	    for (int i = 0; i < 5; i++) {
	        if (guessUsed[i]) continue; 

	        for (int j = 0; j < 5; j++) {
	            if (!targetUsed[j] && guess[i] == target[j]) {
	                letterState[i] = 1; 
	                targetUsed[j] = true; 
	                break; 
	            }
	        }
	    }

	    return letterState; 
	}
	
	public void setAllMatchWords(String[] serverWords) {
	    for (int i = 0; i < 5; i++) {
	        this.sessionWords[i] = serverWords[i]; 
	    }
	}
	

}

