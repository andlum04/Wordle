import javax.swing.*;
import java.awt.*;

public class KeyboardPanel extends JPanel {

    private final WordlePanel wordlePanel;
    private final KeyboardPanelButton[] letters = new KeyboardPanelButton[26];
    public KeyboardPanel(WordlePanel wordlePanel) {
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        addFirstRow();
        addSecondRow();
        addThirdRow();
        this.wordlePanel = wordlePanel;
        wordlePanel.setKeyboardPanel(this);
    }

    public void reset() {
        for (KeyboardPanelButton button : letters) {
            button.reset();
        }
    }

    private void addFirstRow() {
        char[] row = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'};
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        panel.setBackground(Color.BLACK);
        for (char key : row) {
            KeyboardPanelButton button = new KeyboardPanelButton(key, k -> keyPressed(key));
            letters[key - 'A'] = button;
            panel.add(button);
        }
        add(panel);
    }

    private void addSecondRow() {
        char[] row = {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'};
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        panel.setBackground(Color.BLACK);
        for (char key : row) {
            KeyboardPanelButton button = new KeyboardPanelButton(key, k -> keyPressed(key));
            letters[key - 'A'] = button;
            panel.add(button);
        }
        add(panel);
    }

    private void addThirdRow() {
        char[] row = {'Z', 'X', 'C', 'V', 'B', 'N', 'M'};
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        panel.setBackground(Color.BLACK);
        // enter key
        panel.add(new KeyboardPanelButton("ENTER", l -> enterKey(), 65));
        for (char key : row) {
            KeyboardPanelButton button = new KeyboardPanelButton(key, k -> keyPressed(key));
            letters[key - 'A'] = button;
            panel.add(button);
        }
        panel.add(new DeleteButton(l -> deleteKey()));
        add(panel);
    }

    private void keyPressed(char key) {
        wordlePanel.addChar(key);
    }

    private void enterKey() {
        wordlePanel.enter();
    }
    private void deleteKey() {
        wordlePanel.deleteChar();
    }

    public void setColor(char key, int color) {
        letters[key - 'A'].setColor(color);
        Timer timer = new Timer(1500, l -> repaint());
        timer.setRepeats(false);
        timer.start();
    }
}
