import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SolutionsPanel extends JPanel {

    private static final int WIDTH = 170;
    private final JLabel bestWord = new JLabel("SALET");
    private final JLabel possibleSolutionNumber = new JLabel();
    private final JTextArea possibleWords = new JTextArea();
    static final ExecutorService es = Executors.newSingleThreadExecutor();
    private final WordleSolverPanel wordleSolverPanel;
    public SolutionsPanel(WordleSolverPanel wordleSolverPanel) {
        wordleSolverPanel.setSolutionsPanel(this);
        this.wordleSolverPanel = wordleSolverPanel;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Best Word:"), gbc);
        gbc.gridx = 1;
        add(bestWord, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Possible Solutions:"), gbc);
        gbc.gridx = 1;
        updateWords();
        add(possibleSolutionNumber, gbc);
        possibleWords.setFocusable(false);
        possibleWords.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(possibleWords, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.setPreferredSize(new Dimension(WIDTH, 300));
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(scrollPane, gbc);
        gbc.gridy = 3;
        add(new JLabel("Press Enter to get updated results"), gbc);
    }

    public void reset() {
        bestWord.setText("SALET");
        Solver.reset();
        updateWords();
    }

    /**
     * Updates the possible solutions shown
     */
    private void updateWords() {
        possibleSolutionNumber.setText(String.valueOf(Solver.possibleSolutions.size()));
        possibleWords.setText(String.join("\n", Solver.possibleSolutions));
        possibleWords.setCaretPosition(0);
    }

    public void update(String guess, int rule) {
        wordleSolverPanel.stopInput();
        // execute on another thread to avoid freezing the UI
        es.execute(() -> {
            Solver.filter(guess, rule);
            if (Solver.possibleSolutions.size() == 1) {
                // found solution
                SwingUtilities.invokeLater(() -> {
                    String solution = Solver.possibleSolutions.get(0);
                    possibleSolutionNumber.setText("1");
                    wordleSolverPanel.writeSolution(solution);
                    bestWord.setText(solution);
                    possibleWords.setText(solution);
                    JOptionPane.showMessageDialog(getRootPane(),
                            "The answer is " + solution + ". Press " + WordleSolverPanel.COMMAND + "-R to restart",
                            "Solution Found", JOptionPane.PLAIN_MESSAGE);
                });
            } else if (Solver.possibleSolutions.size() < 1) {
                // no solutions
                SwingUtilities.invokeLater(() -> {
                    possibleSolutionNumber.setText("0");
                    bestWord.setText("no solution");
                    possibleWords.setText("no solution");
                    JOptionPane.showMessageDialog(getRootPane(),
                            "No words matching criteria. Press " + WordleSolverPanel.COMMAND + "-R to restart",
                            "No solutions", JOptionPane.ERROR_MESSAGE);
                });
            } else {
                String best = Solver.getBest();
                String wordsLeft = String.join("\n", Solver.possibleSolutions);
                SwingUtilities.invokeLater(() -> {
                    bestWord.setText(best);
                    possibleSolutionNumber.setText(String.valueOf(Solver.possibleSolutions.size()));
                    possibleWords.setText(wordsLeft);
                    possibleWords.setCaretPosition(0);
                    wordleSolverPanel.enableInput();
                });
            }
        });
    }
}
