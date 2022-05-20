import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WordlePanel extends JPanel implements KeyListener {
    final static LetterPane[][] panes = new LetterPane[6][5];
    int currentRow = 0;
    int currentCol = 0;

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
        setPreferredSize(new Dimension(LetterPane.TOTAL_WIDTH * 5, LetterPane.TOTAL_HEIGHT * 6));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (currentCol != 0) {
                currentCol--;
                panes[currentRow][currentCol].setLetter(' ');
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            StringBuilder sb = new StringBuilder();
            for (LetterPane lp : panes[currentRow]) {
                sb.append(lp.getLetter());
            }
            String guess = sb.toString();
            if (Main.wordSet.contains(guess)) {
                // evaluate word
                int[] scratch = new int[26];
                int color = Utility.getGroup(guess, Main.solution, scratch);
                boolean isDone = color == 242;
                for (int i = 4; i >= 0; i--) {
                    panes[currentRow][i].setCurrentState(color % 3);
                    color /= 3;
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
                    JOptionPane.showMessageDialog(this, "Correct word: " + Main.solution, "Good Try!", JOptionPane.INFORMATION_MESSAGE);
                }
                currentRow++;
                currentCol = 0;
            } else {
                panes[currentRow][0].shake();
            }
        } else if (Character.isLetter(e.getKeyChar()) && currentCol != 5) {
            panes[currentRow][currentCol].setLetter(Character.toUpperCase(e.getKeyChar()));
            currentCol++;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
