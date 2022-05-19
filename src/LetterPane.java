import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LetterPane extends JComponent {

    private static final int WIDTH = 62;
    private static final int HEIGHT = 62;
    private static final Dimension DIMENSION = new Dimension(WIDTH, HEIGHT);
    private static final Color GREEN = new Color(0x538d4e);
    private static final Color GRAY = new Color(0x3a3a3c);
    private static final Color LIGHT_GRAY = new Color(0x565758);
    private static final Color YELLOW = new Color(0xb59f3b);
    private static final Font FONT = new Font("Arial", Font.BOLD, 40);

    private static final double INC = Math.PI / 12;
    public enum State {
        UNEVALUATED,
        NONEXISTENT,
        WRONG_PLACE,
        CORRECT_PLACE,
    }

    private char letter = ' ';
    private State currentState = State.UNEVALUATED;
    private final BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private double transform = 0;
    private boolean flipping = false;
    private LetterPane next;
    private boolean shaking = false;
    private boolean resizing = false;
    private final Timer shakeTimer = new Timer(20, (e) -> {
        if (transform >= Math.PI * 6) {
            ((Timer) e.getSource()).stop();
            transform = 0;
            stopShake();
            return;
        }
        doShake();
    });

    private final Timer secondFlip = new Timer(20, (e) -> {
        if (transform >= Math.PI * 2) {
            // stop
            ((Timer)e.getSource()).stop();
            transform = 0;
            flipping = false;
            return;
        }
        transform += INC;
        repaint();
    });

    private final Timer startFlip = new Timer(20, (e) -> {
        if (transform >= Math.PI) {
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
        if (transform >= 10) {
            resizing = false;
            transform = 0;
            ((Timer) e.getSource()).stop();
            repaint();
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
        if (flipping) {
            double amount = (Math.cos(transform) + 1) / 2;
            g2d.translate(0, HEIGHT * (1 - amount) / 2);
            g2d.scale(1, amount);
        } else if (shaking) {
            double amount = Math.cos(transform);
            g2d.translate(10 * amount, 0);
        } else if (resizing) {
            double amount = Utility.normalPdf(5, 2, transform);
            g2d.translate(-WIDTH * amount / 2, -HEIGHT * amount / 2);
            g2d.scale(1 + amount, 1 + amount);
        }
        g2d.drawImage(img, 0, 0, null);
    }

    private void updateLetter() {
        Graphics g = img.createGraphics();
        switch (currentState) {
            case UNEVALUATED -> {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                if (letter == ' ') g.setColor(GRAY);
                else g.setColor(LIGHT_GRAY);
                g.drawRect(0, 0, WIDTH - 2, HEIGHT - 2);
            }
            case NONEXISTENT -> {
                g.setColor(GRAY);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case WRONG_PLACE -> {
                g.setColor(YELLOW);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
            case CORRECT_PLACE -> {
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
        g.drawString(String.valueOf(letter), x, y);
    }

    public void setLetter(char l) {
        letter = l;
        updateLetter();
        if (letter != ' ') {
            resizing = true;
            startResize.start();
        }
        else repaint();
    }

    public char getLetter() {
        return letter;
    }

    public void setCurrentState(State s) {
        currentState = s;
    }

    public void flip() {
        flipping = true;
        startFlip.start();
    }

    public void setNext(LetterPane next) {
        this.next = next;
    }

    public void shake() {
        shakeTimer.start();
    }

    private void doShake() {
        shaking = true;
        transform += Math.PI / 5;
        repaint();
        if (next != null) next.doShake();
    }

    private void stopShake() {
        shaking = false;
        repaint();
        if (next != null) next.stopShake();
    }
}
