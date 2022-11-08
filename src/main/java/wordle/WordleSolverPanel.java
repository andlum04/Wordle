package wordle;

import javax.swing.*;

public class WordleSolverPanel extends WordlePanel {
    private SolutionsPanel solutionsPanel;

    public WordleSolverPanel() {
        super();
        // enable changing the color
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j].enableClicking();
            }
        }
    }

    @Override
    public void enter() {
        if (disabled) return;
        if (currentCol < 5) {
            panes[currentRow][0].shake();
            return;
        }
        String guess = getWord();
        int rule = 0;
        int[] keyColor = new int[26];
        for (int i = 0; i < 5; i++) {
            rule *= 3;
            panes[currentRow][i].disableClicking();
            int color = panes[currentRow][i].getState();
            rule += color;
            int idx = guess.charAt(i) - 'A';
            keyColor[idx] = Math.max(keyColor[idx], color);
        }
        // set color for keyboard
        for (int i = 0; i < 5; i++) {
            int idx = guess.charAt(i) - 'A';
            keyboardPanel.setColor(guess.charAt(i), keyColor[idx]);
        }
        keyboardPanel.repaint();
        solutionsPanel.update(guess, rule);
        currentRow++;
        currentCol = 0;
        if (currentRow == 6) {
            disabled = true;
            JOptionPane.showMessageDialog(getRootPane(),
                    "Ran out of space. Press " + COMMAND + "-R to restart",
                    "Out of space",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void addChar(char c) {
        if (disabled) return;
        if (currentCol != 5) {
            panes[currentRow][currentCol].setCurrentState(LetterPane.STATE_NONEXISTENT);
            panes[currentRow][currentCol].setLetter(c);
            currentCol++;
        }
    }

    @Override
    public void deleteChar() {
        if (disabled) return;
        if (currentCol != 0) {
            currentCol--;
            panes[currentRow][currentCol].setCurrentState(LetterPane.STATE_UNEVALUATED);
            panes[currentRow][currentCol].setLetter(' ');
        }
    }

    @Override
    public void reset() {
        disabled = false;
        currentRow = 0;
        currentCol = 0;
        solutionsPanel.reset();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j].reset();
                panes[i][j].reEnableClicking();
            }
        }
        keyboardPanel.reset();
        repaint();
    }

    public void setSolutionsPanel(SolutionsPanel sp) {
        solutionsPanel = sp;
    }

    public void writeSolution(String solution) {
        for (int i = 0; i < 5; i++) {
            panes[currentRow][i].setCorrectLetter(solution.charAt(i));
            // set color to green
            keyboardPanel.setColor(solution.charAt(i), 2);
        }
        repaint();
    }

    public void stopInput() {
        disabled = true;
    }

    public void enableInput() {
        disabled = false;
    }
}
