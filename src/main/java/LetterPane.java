import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LetterPane extends JComponent {

    public static final int WIDTH = 62;
    public static final int HEIGHT = 62;
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_FLIP = 1;
    public static final int EFFECT_SHAKE = 2;
    public static final int EFFECT_RESIZE = 3;
    public static final int STATE_UNEVALUATED = 3;
    public static final int STATE_NONEXISTENT = 0;
    public static final int STATE_WRONG_PLACE = 1;
    public static final int STATE_CORRECT_PLACE = 2;
    private static final int MARGIN = 3;
    public static final int TOTAL_WIDTH = WIDTH + 2 * MARGIN;
    public static final int TOTAL_HEIGHT = HEIGHT + 2 * MARGIN;
    private static final Color GREEN = new Color(0x538d4e);
    private static final Color GRAY = new Color(0x3a3a3c);
    private static final Color LIGHT_GRAY = new Color(0x565758);
    private static final Color YELLOW = new Color(0xb59f3b);
    private static final Font FONT = new Font("Arial", Font.BOLD, 32);
    private static final double INC = Math.PI / 25;
    private final int x;
    private final int y;
    private char letter = ' ';
    private int currentState = STATE_UNEVALUATED;
    private int newState;
    private double transform = 0;
    private boolean clickingEnabled = false;
    private LetterPane next;
    private long start;
    private final Timer jump2 = new Timer(17, (e) -> {
        if (transform >= 1) {
            ((Timer) e.getSource()).stop();
            transform = 0;
            resetBounds();
            repaint();
            return;
        }
        transform = (double) (System.currentTimeMillis() - start) / 20 * 0.04;
        jumpBounds();
        repaint();
    });
    private final Timer jump = new Timer(17, (e) -> {
        transform = (double) (System.currentTimeMillis() - start) / 20 * 0.04;
        jumpBounds();
        repaint();
        if (transform >= 0.25) {
            // stop
            ((Timer) e.getSource()).stop();
            if (next != null) next.startJump();
            jump2.start();
        }
    });
    private int effect;
    private final Timer secondFlip = new Timer(17, (e) -> {
        if (transform >= Math.PI) {
            // stop
            ((Timer) e.getSource()).stop();
            transform = 0;
            effect = EFFECT_NONE;
            repaint();
            return;
        }
        transform = (double) (System.currentTimeMillis() - start) / 20 * INC;
        repaint();
    });
    private final Timer startFlip = new Timer(17, (e) -> {
        transform = (double) (System.currentTimeMillis() - start) / 20 * INC;
        repaint();
        if (transform >= Math.PI / 2) {
            ((Timer) e.getSource()).stop();
            currentState = newState;
            secondFlip.start();
            if (next != null) next.flip();
        }
    });
    private final Timer startResize = new Timer(17, (e) -> {
        if (transform >= 5) {
            effect = EFFECT_NONE;
            transform = 0;
            ((Timer) e.getSource()).stop();
            repaint();
            return;
        }
        transform = (double) (System.currentTimeMillis() - start) / 20;
        repaint();
    });
    private final Timer shakeTimer = new Timer(17, (e) -> {
        if (transform >= Math.PI * 12) {
            ((Timer) e.getSource()).stop();
            stopShake();
            return;
        }
        doShake(System.currentTimeMillis() - start);
    });

    public LetterPane(int x, int y) {
        this.x = x * TOTAL_WIDTH;
        this.y = y * TOTAL_HEIGHT + 30;
        setBounds(this.x, this.y, TOTAL_WIDTH, TOTAL_HEIGHT);
    }

    private void jumpBounds() {
        setLocation(x, y - (int) Math.round(Math.exp(-25 * transform * transform + 25 * transform - 6.25) * (-3 * transform + 1.8) * HEIGHT));
    }

    private void resetBounds() {
        setLocation(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.translate(MARGIN, MARGIN);
        switch (effect) {
            case EFFECT_FLIP -> {
                double amount = Math.abs(Math.cos(transform));
                g2d.translate(0, HEIGHT * (1 - amount) / 2);
                g2d.scale(1, amount);
            }
            case EFFECT_SHAKE -> {
                double amount = Math.sin(transform) * Utility.normalPdf(Math.PI * 6, 6, transform) * 7;
                g2d.translate(10 * amount, 0);
            }
            case EFFECT_RESIZE -> {
                double amount = Utility.normalPdf(2.5, 1, transform);
                g2d.translate(-WIDTH * amount / 2, -HEIGHT * amount / 2);
                g2d.scale(1 + amount, 1 + amount);
            }
        }
        switch (currentState) {
            case STATE_UNEVALUATED -> {
                if (letter == ' ') g.setColor(GRAY);
                else g2d.setColor(LIGHT_GRAY);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(1, 1, WIDTH - 2, HEIGHT - 2);
            }
            case STATE_NONEXISTENT -> {
                g2d.setColor(GRAY);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case STATE_WRONG_PLACE -> {
                g2d.setColor(YELLOW);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case STATE_CORRECT_PLACE -> {
                g2d.setColor(GREEN);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }
        FontMetrics metrics = g2d.getFontMetrics(FONT);
        String text = String.valueOf(letter);
        // Determine the X coordinate for the text
        int x = (WIDTH - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = (HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();
        // Set the font
        g.setColor(Color.WHITE);
        g.setFont(FONT);
        g.drawString(text, x, y);
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char l) {
        letter = l;
        if (letter != ' ') {
            effect = EFFECT_RESIZE;
            start = System.currentTimeMillis();
            startResize.start();
        } else repaint();
    }

    /**
     * Sets the letter, while also changing color to green. Note that this does not repaint because it is expected
     * that the parent panel does it.
     * @param l the letter
     */
    public void setCorrectLetter(char l) {
        letter = l;
        currentState = STATE_CORRECT_PLACE;
    }

    /**
     * To set the future state of the component. Flip should be called after this
     * @param s the state
     */
    public void setNewState(int s) {
        newState = s;
    }

    public void setCurrentState(int s) {
        currentState = s;
    }

    public void flip() {
        effect = EFFECT_FLIP;
        start = System.currentTimeMillis();
        startFlip.start();
    }

    public void setNext(LetterPane next) {
        this.next = next;
    }

    public void shake() {
        start = System.currentTimeMillis();
        shakeTimer.start();
    }

    private void doShake(long time) {
        effect = EFFECT_SHAKE;
        transform = (double) time / 20 * Math.PI / 3;
        repaint();
        if (next != null) next.doShake(time);
    }

    private void stopShake() {
        effect = EFFECT_NONE;
        repaint();
        transform = 0;
        if (next != null) next.stopShake();
    }

    public void startJump() {
        start = System.currentTimeMillis();
        jump.start();
    }

    public void reset() {
        letter = ' ';
        currentState = STATE_UNEVALUATED;
    }

    public int getState() {
        return currentState;
    }

    public void enableClicking() {
        clickingEnabled = true;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!clickingEnabled) return;
                currentState = switch (currentState) {
                    case STATE_NONEXISTENT -> STATE_WRONG_PLACE;
                    case STATE_WRONG_PLACE -> STATE_CORRECT_PLACE;
                    case STATE_CORRECT_PLACE -> STATE_NONEXISTENT;
                    // if it is unevaluated, it stays that way;
                    default -> STATE_UNEVALUATED;
                };
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void reEnableClicking() {
        clickingEnabled = true;
    }

    public void disableClicking() {
        clickingEnabled = false;
    }
}
