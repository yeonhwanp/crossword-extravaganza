package crossword;

/**
 * An immutable player of memory scramble.
 * Threadsafe.
 *
 */
public class Player {
    
    // Abstraction function:
    //   AF(id) = represents an immutable player of our game with the id ID
    // Representation invariant:
    //   id only contains alphanumeric characters
    // Safety from rep exposure:
    //   All fields are private, final and immutable. Methods only ever return immutable objects, and all inputs to methods are also immutable so it;s safe
    //   to directly alias them.
    // Thread safety argument:
    //   Player is immutable, and the fields are all final and immutable types, so it is thread safe. (since it cannot be mutated, so we don't risk violating the rep invariant)
    
    private final String id;

    /**
     * Construct a new player object.
     * @param pID the ID of the player
     */
    public Player(String pID) {
        id = pID;
        checkRep();
    }
    
    private void checkRep() {
        assert id != null;
        assert id.matches("^[a-zA-Z0-9]+$");
    }
    
    /**
     * Get the ID of the player
     * @return the ID of the player
     */
    public String getID() {
        return id;
    }
    
    /**
     * Performs observational equality between two objects.
     * @return true iff the IDs of the two Player objects match exactly
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Player) {
            Player otherPlayer = (Player) o;
            
            return this.getID().equals(otherPlayer.getID());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}