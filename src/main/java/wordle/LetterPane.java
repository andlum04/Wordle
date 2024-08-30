package wordle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Field;

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
    private static final double CAM_DIST = 1000;
    private static final double INPUT_SCALE = 1.2;
    private static final int SCALE;
    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            Field field = device.getClass().getDeclaredField("scale");
            SCALE = unsafe.getInt(device, unsafe.objectFieldOffset(field));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
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
        if (effect != EFFECT_FLIP) {
            g2d.translate(MARGIN, MARGIN);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            draw(g2d);
            return;
        }
        // flip effect
        BufferedImage img = new BufferedImage((int)(INPUT_SCALE*WIDTH*SCALE), (int)(INPUT_SCALE*HEIGHT*SCALE), BufferedImage.TYPE_INT_ARGB);
        BufferedImage outputImg = new BufferedImage(SCALE * TOTAL_WIDTH, SCALE * TOTAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = img.createGraphics();
        ig.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ig.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ig.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        ig.scale(SCALE*INPUT_SCALE, SCALE*INPUT_SCALE);
        draw(ig);
        int[] inputBuffer = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
        int[] outputBuffer = ((DataBufferInt)outputImg.getRaster().getDataBuffer()).getData();
        double cosA = Math.abs(Math.cos(transform));
        double sinA = Math.sin(transform);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                double unscaledX = x / INPUT_SCALE - WIDTH*SCALE/2.0;
                double unscaledY = y / INPUT_SCALE - HEIGHT*SCALE/2.0;
                double z = CAM_DIST * SCALE;
                double zOffset = unscaledY * sinA;
                if (transform < Math.PI / 2) {
                    z += zOffset;
                } else {
                    z -= zOffset;
                }
                double factor = CAM_DIST * SCALE / z;
                int newX = (int)(unscaledX * factor + (WIDTH / 2.0 + MARGIN) * SCALE);
                int newY = (int)(unscaledY * cosA * factor + (HEIGHT / 2.0 + MARGIN) * SCALE);
                if (newX >= 0 && newX < SCALE * TOTAL_WIDTH && newY >= 0 && newY < SCALE * TOTAL_HEIGHT) {
                    outputBuffer[newX + newY * SCALE * TOTAL_WIDTH] = inputBuffer[x + y * img.getWidth()];
                }
            }
        }
        g2d.drawImage(outputImg, 0, 0, TOTAL_WIDTH, TOTAL_HEIGHT, null);
    }

    private void draw(Graphics2D g2d) {
        switch (effect) {
            // Flip not defined here because it's not a simple linear transformation
//            case EFFECT_FLIP -> {
//                double amount = Math.abs(Math.cos(transform));
//                g2d.translate(0, HEIGHT * (1 - amount) / 2);
//                g2d.scale(1, amount);
//            }
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
                if (letter == ' ') g2d.setColor(GRAY);
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
        g2d.setColor(Color.WHITE);
        g2d.setFont(FONT);
        g2d.drawString(text, x, y);
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
                if (!clickingEnabled || currentState == STATE_UNEVALUATED) return;
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
