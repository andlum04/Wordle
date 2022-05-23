import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class DeleteButton extends JButton {

    private static final Color BUTTON_COLOR = new Color(0x818384);
    private static final int WIDTH = 65;
    private static final int HEIGHT = 58;

    private static final Icon ICON;

    static {
        try {
            ICON = new SVGIcon("delete.svg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DeleteButton(ActionListener l) {
        addActionListener(l);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorderPainted(false);
        setFocusable(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BUTTON_COLOR);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 6,6);
        ICON.paintIcon(this, g, 20, 17);
    }
}
