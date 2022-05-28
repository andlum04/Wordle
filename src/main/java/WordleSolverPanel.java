public class WordleSolverPanel extends WordlePanel {
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
        if (currentCol < 5) return;
        currentRow++;
        if (currentRow == 5) {
            disabled = true;
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
}
