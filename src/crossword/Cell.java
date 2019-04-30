package crossword;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

public class Cell {
    
    /*
     * TODO AF, RI, SRE, etc.
     */

    private final int row; // (0-indexed)
    private final int col; 
    private String value;
    private final List<Word> correspondingWords;
    
    public Cell(int pRow, int pCol) {
        row = pRow;
        col = pCol;
        value = ""; // empty character represents no character there
        correspondingWords = new ArrayList<Word>();
        
        checkRep();
    }
    
    private void checkRep() {
        // TODO
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public boolean changeValue(char pValue, Player player) {
        if(canChangeValue(player))
        {
            value = String.valueOf(pValue);
            return true;
        }
        
        return false;
    }
    
    public boolean clearValue(Player player) {
        if(canChangeValue(player))
        {
            value = "";
            return true;
        }
        
        return false;
    }
    
    public String getValue() {
        return value;
    }
    
    public void addWord(Word word) {
        correspondingWords.add(word);
    }
    
    public boolean canChangeValue(Player player) {
        throw new RuntimeException("not done implementing");
    }
}
