package wordle;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Objects;

public class SVGIcon extends ImageIcon {

    private static final SVGUniverse svgUniverse = new SVGUniverse();
    private final SVGDiagram diagram;
    private final int width;
    private final int height;
    private final double scale;

    public SVGIcon(String resource, int height) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(SVGIcon.class.getClassLoader().getResourceAsStream(resource)))) {
            URI uri = svgUniverse.loadSVG(reader, resource);
            diagram = svgUniverse.getDiagram(uri);
        }
        scale = (double)height / diagram.getHeight();
        width = (int)(diagram.getWidth() * scale);
        this.height = height;
    }

    public SVGIcon(String resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(SVGIcon.class.getClassLoader().getResourceAsStream(resource)))) {
            URI uri = svgUniverse.loadSVG(reader, resource);
            diagram = svgUniverse.getDiagram(uri);
        }
        scale = 1;
        width = (int) diagram.getWidth();
        height = (int) diagram.getHeight();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    private void paintSVG(Graphics2D g, int x, int y) {
        // antialias
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (diagram == null) {
            paintSVGError(g, x, y);
            return;
        }
        g.translate(x, y);
        g.clipRect(0, 0, getIconWidth(), getIconHeight());
        g.scale(scale, scale);
        diagram.setIgnoringClipHeuristic(true);
        try {
            diagram.render(g);
        } catch (SVGException e) {
            paintSVGError(g, 0, 0);
        }
    }

    private void paintSVGError(Graphics g, int x, int y) {
        g.setColor(Color.RED);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        paintSVG((Graphics2D) g, x, y);
    }
}
