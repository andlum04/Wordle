package wordle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WordlePanel extends JPanel implements KeyListener {
    protected static final String COMMAND;
    protected static final int CTRL_KEYCODE;
    static {
         if (System.getProperty("os.name").equals("Mac OS X")) {
             // Mac users like to use command key
             COMMAND = "⌘";
             CTRL_KEYCODE = KeyEvent.VK_META;
         } else {
             // windows/linux/etc
             COMMAND = "CTRL";
             CTRL_KEYCODE = KeyEvent.VK_CONTROL;
         }
    }
    protected final LetterPane[][] panes = new LetterPane[6][5];
    protected int currentRow = 0;
    protected int currentCol = 0;
    protected boolean disabled = false;
    protected boolean controlPressed = false;

    protected KeyboardPanel keyboardPanel;

    public WordlePanel() {
        // I don't need a layout!
        setLayout(null);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j] = new LetterPane(j, i);
                add(panes[i][j]);
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                panes[i][j].setNext(panes[i][j+1]);
            }
        }
        addKeyListener(this);
        setPreferredSize(new Dimension(LetterPane.TOTAL_WIDTH * 5, LetterPane.TOTAL_HEIGHT * 6 + 45));
    }

    public void reset() {
        disabled = false;
        currentRow = 0;
        currentCol = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j].reset();
                setComponentZOrder(panes[i][j], 0);
            }
        }
        Main.setSolution();
        keyboardPanel.reset();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (controlPressed && e.getKeyCode() == KeyEvent.VK_R) {
            // reset when control-r is pressed
            reset();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            deleteChar();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            enter();
        } else if (Character.isLetter(e.getKeyChar())) {
            addChar(Character.toUpperCase(e.getKeyChar()));
        } else if (e.getKeyCode() == CTRL_KEYCODE) {
            controlPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == CTRL_KEYCODE) {
            controlPressed = false;
        }
    }

    public void addChar(char c) {
        if (disabled) return;
        if (currentCol != 5) {
            panes[currentRow][currentCol].setLetter(c);
            currentCol++;
        }
    }

    public void deleteChar() {
        if (disabled) return;
        if (currentCol != 0) {
            currentCol--;
            panes[currentRow][currentCol].setLetter(' ');
        }
    }

    public void enter() {
        if (disabled) return;
        String guess = getWord();
        if (Main.wordSet.contains(guess)) {
            // evaluate word
            int[] scratch = new int[26];
            int colors = Utility.getGroup(guess, Main.solution, scratch);
            boolean isDone = colors == 242;
            int[] keyColor = new int[26];
            for (int i = 4; i >= 0; i--) {
                int color = colors % 3;
                panes[currentRow][i].setNewState(color);
                int idx = guess.charAt(i) - 'A';
                keyColor[idx] = Math.max(keyColor[idx], color);
                colors /= 3;
            }
            // set color for keyboard
            for (int i = 0; i < 5; i++) {
                int idx = guess.charAt(i) - 'A';
                keyboardPanel.setColor(guess.charAt(i), keyColor[idx]);
            }
            // repaint keyboard after 1.5 seconds
            Timer timer = new Timer(1500, l -> keyboardPanel.repaint());
            timer.setRepeats(false);
            timer.start();
            panes[currentRow][0].flip();
            int rowToJump = currentRow;
            if (isDone) {
                // don't let user input anything further
                disabled = true;
                for (int i = 0; i < 5; i++) {
                    // move panes to front
                    setComponentZOrder(panes[currentRow][i], 1);
                }
                // start jumping
                Timer t = new Timer(1500, l -> panes[rowToJump][0].startJump());
                t.setRepeats(false);
                t.start();
                Timer t2 = new Timer(2500, l -> {
                    JOptionPane.showMessageDialog(this,
                                "Congrats! Press " + COMMAND + "-R to restart",
                                "Congratulations",
                                JOptionPane.PLAIN_MESSAGE);
                    requestFocus();
                });
                t2.setRepeats(false);
                t2.start();
            } else if (currentRow == 5) {
                disabled = true;
                Timer t = new Timer(1500, l -> {
                    JOptionPane.showMessageDialog(this,
                                "Correct word: " + Main.solution + ". Press " + COMMAND + "-R to restart",
                                "Good Try!",
                                JOptionPane.PLAIN_MESSAGE);
                    requestFocus();
                });
                t.setRepeats(false);
                t.start();
            }
            currentRow++;
            currentCol = 0;
        } else {
            panes[currentRow][0].shake();
        }
    }

    public String getWord() {
        StringBuilder sb = new StringBuilder();
        for (LetterPane lp : panes[currentRow]) {
            sb.append(lp.getLetter());
        }
        return sb.toString();
    }

    public void setKeyboardPanel(KeyboardPanel keyboardPanel) {
        this.keyboardPanel = keyboardPanel;
    }

    /**
     * Tells program to release the control key
     */
    public void releaseControl() {
        controlPressed = false;
    }
}
