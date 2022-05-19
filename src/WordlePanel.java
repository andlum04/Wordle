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
        setLayout(new GridLayout(6, 5, 4, 4));
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j] = new LetterPane();
                add(panes[i][j]);
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                panes[i][j].setNext(panes[i][j+1]);
            }
        }
        addKeyListener(this);
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
                for (int i = 4; i >= 0; i--) {
                    panes[currentRow][i].setCurrentState(switch (color % 3) {
                        case 0 -> LetterPane.State.NONEXISTENT;
                        case 1 -> LetterPane.State.WRONG_PLACE;
                        case 2 -> LetterPane.State.CORRECT_PLACE;
                        default -> throw new IllegalStateException("Unexpected value: " + color % 3);
                    });
                    color /= 3;
                }
                panes[currentRow][0].flip();
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
