package crossword;

/**
 * Immutable data type to represent word class
 * @author christophercheung
 *
 */
public class WordTuple {

    private final String word;
    private final String hint;
    private final String direction;
    private final int startRow;
    private final int startCol;
    private final int id;
    
    public WordTuple(final int pRow, final int pCol, final String pHint, final int pID, final String pWord, final String pDirection) {
        this.word = pWord;
        this.hint = pHint;
        this.direction = pDirection;
        this.startRow = pRow;
        this.startCol = pCol;
        this.id = pID;
    }
    
    public String getWord() {
        return word;
    }
    
    public String getHint() {
        return hint;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public int getRow() {
        return startRow;
    }
    
    public int getCol() {
        return startCol;
    }
    
    public int getID() {
        return id;
    }
    
    @Override
    /**
     * Checks equality of word tuple objects
     * @param that other object to check
     * @return if word tuples are equal
     */
    public boolean equals(Object that) {
        return that instanceof WordTuple &&
                (((WordTuple) that).getWord().equals(this.getWord())) &&
                (((WordTuple) that).getHint().equals(this.getHint())) &&
                (((WordTuple) that).getDirection().equals(this.getDirection())) &&
                (((WordTuple) that).getRow() == this.getRow()) &&
                (((WordTuple) that).getCol() == this.getCol()) &&
                (((WordTuple) that).getID() == this.getID());
    }
    
    @Override
    /**
     * Get hashcode of this word tuple
     * @return haschode of word tuple
     */
    public int hashCode() {
        return getAscii(getWord())*21 + getAscii(getHint())*11 + getAscii(getDirection())*9
                + getRow()*7 + getCol()*8 + getID()*6;
    }
    
    
    /**
     * Get ascii value of string
     * @param str string to get value of
     * @return ascii value of string
     */
    private static int getAscii(String str) {
        int count = 0;
        //add ascii value times (index + 1) for total sum
        for (int i = 0; i < str.length(); i++) {
            char character = str.charAt(i);
            int ascii = (int) character;
            count += (i+1) * ascii;
        }
        return count;
    }
}
