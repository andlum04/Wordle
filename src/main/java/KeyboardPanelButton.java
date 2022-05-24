import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class KeyboardPanelButton extends JButton {
    private static final Color GREEN = new Color(0x538d4e);
    private static final Color GRAY = new Color(0x3a3a3c);
    private static final Color LIGHT_GRAY = new Color(0x818384);
    private static final Color YELLOW = new Color(0xb59f3b);
    private static final int WIDTH = 43;
    private static final int HEIGHT = 58;
    private static final Font FONT = new Font("Arial", Font.BOLD, 13);

    public static final int STATE_UNEVALUATED = 3;
    public static final int STATE_NONEXISTENT = 0;
    public static final int STATE_WRONG_PLACE = 1;
    public static final int STATE_CORRECT_PLACE = 2;

    private Color color = LIGHT_GRAY;

    public KeyboardPanelButton(char c, ActionListener l) {
        super(String.valueOf(c));
        addActionListener(l);
        setBorderPainted(false);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(false);
    }

    public KeyboardPanelButton(String text, ActionListener l, int width) {
        super(String.valueOf(text));
        addActionListener(l);
        setBorderPainted(false);
        setPreferredSize(new Dimension(width, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(false);
    }

    public void setColor(int color) {
        this.color = switch (color) {
            case STATE_UNEVALUATED -> LIGHT_GRAY;
            case STATE_NONEXISTENT -> GRAY;
            case STATE_CORRECT_PLACE -> GREEN;
            case STATE_WRONG_PLACE -> YELLOW;
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

    @Override
    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 6,6);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics(FONT);
        String text = getText();
        // Determine the X coordinate for the text
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        // Set the font
        g.setColor(Color.WHITE);
        g.setFont(FONT);
        g.drawString(text, x, y);
    }

    public void reset() {
        color = LIGHT_GRAY;
    }
}
