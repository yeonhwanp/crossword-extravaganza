package crossword;

/**
 * Immutable data type to represent word class
 * @author christophercheung
 *
 */
public class WordTuple {

    private final String word; //TODO check restrictions on these guys
    private final String hint;
    private final String direction;
    private final int startRow;
    private final int startCol;
    
    
    /*
     * Abstraction Function:
     * AF(word, hint, direction, startRow, startCol) = Word tuple that is the word word, with an associated hint hint to
     * help users guess the word, in the direction direction, starting at row startRow and column startCol
     * 
     * Rep Invariant:
     *  direction must be ACROSS or DOWN
     *  startRow and startCol must be non-negative
     * 
     * Safety from rep exposure:
     *  all of our fields are private and final and immutable, therefore no rep exposure
     *  none of our fields are saved in any of our methods
     *  
     * Thread safety argument:
     *  We are using an immutable type, WordTuple. Since the fields are all private and final and immutable, there is no
     *  way to have thread safety threatened.
     */
    
    /**
     * Create a word tuple
     * @param pRow row that word starts in
     * @param pCol column that word starts in
     * @param pHint hint that to help players guess word
     * @param pWord actual word that is to be guessed
     * @param pDirection direction of the word
     */
    public WordTuple(final int pRow, final int pCol, final String pHint, final String pWord, final String pDirection) {
        this.word = pWord;
        this.hint = pHint;
        this.direction = pDirection;
        this.startRow = pRow;
        this.startCol = pCol;
        checkRep();
    }
    
    /**
     * Check for valid rep
     */
    private void checkRep() {
        assert direction.equals("ACROSS") || direction.equals("DOWN");
        assert startRow >= 0 && startCol >= 0;
    }
    
    /**
     * Get the actual, correct word itself
     * @return correct word
     */
    public String getWord() {
        return word;
    }
    
    /**
     * Get the associated hint with this word
     * @return associated int
     */
    public String getHint() {
        return hint;
    }
    
    /**
     * Get the direction that the word goes in
     * @return direction of the word
     */
    public String getDirection() {
        return direction;
    }
    
    /**
     * Get the row that the word starts in
     * @return the starting row
     */
    public int getRow() {
        return startRow;
    }
    
    /**
     * Get the column that the word starts in
     * @return the starting column
     */
    public int getCol() {
        return startCol;
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
                (((WordTuple) that).getCol() == this.getCol());
    }
    
    @Override
    /**
     * Get hashcode of this word tuple
     * @return haschode of word tuple
     */
    public int hashCode() {
        return getAscii(getWord())*21 + getAscii(getHint())*11 + getAscii(getDirection())*9
                + getRow()*7 + getCol()*8;
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
