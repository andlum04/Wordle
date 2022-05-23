import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WordlePanel extends JPanel implements KeyListener {
    private final LetterPane[][] panes = new LetterPane[6][5];
    private int currentRow = 0;
    private int currentCol = 0;

    private KeyboardPanel keyboardPanel;

    public WordlePanel() {
        setBackground(Color.BLACK);
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
        setPreferredSize(new Dimension(LetterPane.TOTAL_WIDTH * 5, LetterPane.TOTAL_HEIGHT * 6 + 15));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            deleteChar();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            enter();
        } else if (Character.isLetter(e.getKeyChar())) {
            addChar(Character.toUpperCase(e.getKeyChar()));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void addChar(char c) {
        if (currentCol != 5) {
            panes[currentRow][currentCol].setLetter(c);
            currentCol++;
        }
    }

    public void deleteChar() {
        if (currentCol != 0) {
            currentCol--;
            panes[currentRow][currentCol].setLetter(' ');
        }
    }

    public void enter() {
        StringBuilder sb = new StringBuilder();
        for (LetterPane lp : panes[currentRow]) {
            sb.append(lp.getLetter());
        }
        String guess = sb.toString();
        if (Main.wordSet.contains(guess)) {
            // evaluate word
            int[] scratch = new int[26];
            int colors = Utility.getGroup(guess, Main.solution, scratch);
            boolean isDone = colors == 242;
            for (int i = 4; i >= 0; i--) {
                int color = colors % 3;
                panes[currentRow][i].setCurrentState(color);
                keyboardPanel.setColor(panes[currentRow][i].getLetter(), color);
                colors /= 3;
            }
            panes[currentRow][0].flip();
            int rowToJump = currentRow;
            if (isDone) {
                // don't let user input anything further
                removeKeyListener(this);
                for (int i = 0; i < 5; i++) {
                    // move panes to front
                    setComponentZOrder(panes[currentRow][i], 1);
                }
                // start jumping
                Timer t = new Timer(1500, l -> panes[rowToJump][0].startJump());
                t.setRepeats(false);
                t.start();
            } else if (currentRow == 5) {
                removeKeyListener(this);
                Timer t = new Timer(1500, l -> JOptionPane.showMessageDialog(this, "Correct word: " + Main.solution, "Good Try!", JOptionPane.INFORMATION_MESSAGE));
                t.setRepeats(false);
                t.start();
            }
            currentRow++;
            currentCol = 0;
        } else {
            panes[currentRow][0].shake();
        }
    }

    public void setKeyboardPanel(KeyboardPanel keyboardPanel) {
        this.keyboardPanel = keyboardPanel;
    }
}