package crossword;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

public class Cell {
    
    /*
     * TODO AF, RI, SRE, etc.
     */
    
    public enum Exist {PRESENT, ABSENT}

    private final int row; // (0-indexed)
    private final int col; 
    private String value;
    private final List<Word> correspondingWords;
    private final Exist existState;
    
    private static final String EMPTY_CELL = " ";
    
    // Methods also need a toString()
    
    public Cell(int pRow, int pCol, Exist state) {
        row = pRow;
        col = pCol;
        value = EMPTY_CELL; // empty cell represents no character there
        correspondingWords = new ArrayList<Word>();
        existState = state;
        
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
    
    public boolean isPresent() {
        return existState == Exist.PRESENT;
    }
    
    public boolean isAbsent() {
        return existState == Exist.ABSENT;
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
            value = EMPTY_CELL;
            return true;
        }
        
        return false;
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean isEmpty() {
        throw new RuntimeException("not done implementing");
    }
    
    public void addWord(Word word) {
        correspondingWords.add(word);
    }
    
    public boolean canChangeValue(Player player) {
        throw new RuntimeException("not done implementing");
    }
    
    @Override
    public String toString() {
        if(isAbsent()) {
            return "#";
        }
        return getValue();
    }
}
