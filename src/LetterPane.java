import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LetterPane extends JComponent {

    private static final int WIDTH = 62;
    private static final int HEIGHT = 62;
    private static final int MARGIN = 2;
    private static final Dimension DIMENSION = new Dimension(WIDTH + 2 * MARGIN, HEIGHT + 2 * MARGIN);
    private static final Color GREEN = new Color(0x538d4e);
    private static final Color GRAY = new Color(0x3a3a3c);
    private static final Color LIGHT_GRAY = new Color(0x565758);
    private static final Color YELLOW = new Color(0xb59f3b);
    private static final Font FONT = new Font("Arial", Font.BOLD, 32);

    private static final double INC = Math.PI / 24;

    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_FLIP = 1;
    public static final int EFFECT_SHAKE = 2;
    public static final int EFFECT_RESIZE = 3;
    public static final int EFFECT_JUMP = 4;
    public static final int STATE_UNEVALUATED = 3;
    public static final int STATE_NONEXISTENT = 0;
    public static final int STATE_WRONG_PLACE = 1;
    public static final int STATE_CORRECT_PLACE = 2;

    private char letter = ' ';
    private int currentState = STATE_UNEVALUATED;
    private final BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
    private double transform = 0;
    private int effect;
    private LetterPane next;
    private final Timer shakeTimer = new Timer(20, (e) -> {
        if (transform >= Math.PI * 12) {
            ((Timer) e.getSource()).stop();
            stopShake();
            return;
        }
        doShake();
    });

    private final Timer jump2 = new Timer(20, (e) -> {
        if (transform >= Math.PI * 2) {
            ((Timer) e.getSource()).stop();
            effect = EFFECT_NONE;
            transform = 0;
            repaint();
            return;
        }
        transform += INC * 2;
        repaint();
    });

    private final Timer jump = new Timer(20, (e) -> {
        if (transform >= Math.PI / 2) {
            // stop
            ((Timer) e.getSource()).stop();
            if (next != null) next.startJump();
            jump2.start();
            return;
        }
        transform += INC * 2;
        repaint();
    });

    private final Timer secondFlip = new Timer(20, (e) -> {
        if (transform >= Math.PI) {
            // stop
            ((Timer)e.getSource()).stop();
            transform = 0;
            effect = EFFECT_NONE;
            return;
        }
        transform += INC;
        repaint();
    });

    private final Timer startFlip = new Timer(20, (e) -> {
        if (transform >= Math.PI / 2) {
            ((Timer)e.getSource()).stop();
            updateLetter();
            secondFlip.start();
            if (next != null) next.flip();
            return;
        }
        transform += INC;
        repaint();
    });

    private final Timer startResize = new Timer(20, (e) -> {
        if (transform >= 5) {
            effect = EFFECT_NONE;
            transform = 0;
            ((Timer) e.getSource()).stop();
            repaint();
            return;
        }
        transform += 1;
        repaint();
    });

    public LetterPane() {
        setPreferredSize(DIMENSION);
        setMinimumSize(DIMENSION);
        updateLetter();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
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
            case EFFECT_JUMP -> {
                double amount = -Math.sin(transform) * WIDTH / 4 - 0.3;
                g2d.translate(0, amount);
            }
        }
        g2d.drawImage(img, MARGIN, MARGIN, null);
    }

    private void updateLetter() {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        switch (currentState) {
            case STATE_UNEVALUATED -> {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                if (letter == ' ') g.setColor(GRAY);
                else g.setColor(LIGHT_GRAY);
                g.setStroke(new BasicStroke(2));
                g.drawRect(1, 1, WIDTH - 2, HEIGHT - 2);
            }
            case STATE_NONEXISTENT -> {
                g.setColor(GRAY);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case STATE_WRONG_PLACE -> {
                g.setColor(YELLOW);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case STATE_CORRECT_PLACE -> {
                g.setColor(GREEN);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }
        FontMetrics metrics = g.getFontMetrics(FONT);
        String text = String.valueOf(letter);
        // Determine the X coordinate for the text
        int x = (WIDTH - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = (HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();
        // Set the font
        g.setColor(Color.WHITE);
        g.setFont(FONT);
        //System.out.println("Drew letter: " + letter);
        g.drawString(text, x, y);
    }

    public void setLetter(char l) {
        letter = l;
        updateLetter();
        if (letter != ' ') {
            effect = EFFECT_RESIZE;
            startResize.start();
        }
        else repaint();
    }

    public char getLetter() {
        return letter;
    }

    public void setCurrentState(int s) {
        currentState = s;
    }

    public void flip() {
        effect = EFFECT_FLIP;
        startFlip.start();
    }

    public void setNext(LetterPane next) {
        this.next = next;
    }

    public void shake() {
        shakeTimer.start();
    }

    private void doShake() {
        effect = EFFECT_SHAKE;
        transform += Math.PI / 3;
        repaint();
        if (next != null) next.doShake();
    }

    private void stopShake() {
        effect = EFFECT_NONE;
        repaint();
        transform = 0;
        if (next != null) next.stopShake();
    }

    public void startJump() {
        effect = EFFECT_JUMP;
        jump.start();
    }
}
