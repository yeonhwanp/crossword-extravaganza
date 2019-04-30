/* Copyright (c) 2019 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package crossword;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Crossword game client for server.
 */
public class Client {

    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 900;

    /**
     * Start a Crossword Extravaganza client.
     * 
     * ADDITIONS TO MAIN METHOD FOR BRIAN TO CODE:
     * Given the server address, connect to the server. The server will send over a client's view of a match (toString), which holds
     * length, position, and orientation of the words, as well as their associated hint.
     * 
     * Then, the client should display this information as a puzzle. The information is displayed via CrosswordCanvas
     * 
     * 
     * 
     * @param args The command line arguments should include only the server address.
     */
    public static void main(String[] args) {

        launchGameWindow(/*match*/);

    }
    
    /**
     * Starter code to display a window with a CrosswordCanvas,
     * a text box to enter commands and an Enter button.
     * 
     * @param matchStr toString of client view of a match. Use this to display the puzzle, its hints, and any extra info.
     * Brian - I think all you need to do for the warmup in client is simply display the puzzle and stuff. You shouldn't
     * have to worry too much about input into the textbox, etc.
     * 
     */
    private static void launchGameWindow(String matchStr) {

        CrosswordCanvas canvas = new CrosswordCanvas();
        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener((event) -> {
            // This code executes every time the user presses the Enter
            // button. Recall from reading 24 that this code runs on the
            // Event Dispatch Thread, which is different from the main
            // thread.
            System.out.println();
            canvas.repaint();
        });
        enterButton.setSize(10, 10);

        JTextField textbox = new JTextField(30);
        textbox.setFont(new Font("Arial", Font.BOLD, 20));

        JFrame window = new JFrame("Crossword Client");
        window.setLayout(new BorderLayout());
        window.add(canvas, BorderLayout.CENTER);

        JPanel contentPane = new JPanel();
        contentPane.add(textbox);
        contentPane.add(enterButton);

        window.add(contentPane, BorderLayout.SOUTH);

        window.setSize(CANVAS_WIDTH + 50, CANVAS_HEIGHT + 50);

        window.getContentPane().add(contentPane);

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
