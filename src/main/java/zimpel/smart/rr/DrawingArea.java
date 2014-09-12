package zimpel.smart.rr;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class DrawingArea extends JPanel {

    public enum RectangleOrientation {
        HORIZONTAL,VERTICAL
    }

    private Rectangle[] rectangles = new Rectangle[]{};
    private final RectangleOrientation orientation;


    private Canvas paintArea = new Canvas(){
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            // we need to flip the thing here because 0,0 is in the upper left corner of the Canvas
            AffineTransform at = new AffineTransform();
            at.scale(1.0,-1.0);
            // we also want to move 150 down and place the thing somewhere in the middle
            int width = 0;
            if(orientation == RectangleOrientation.HORIZONTAL) {
                for(Rectangle r : rectangles) {
                    width += r.w;
                }
            } else {
                for(Rectangle r : rectangles) {
                    width = r.w > width ? r.w : width;
                }
            }

            at.translate(getWidth() / 2 - width / 2,-150.0);
            g2d.setTransform(at);
            for(Rectangle rectangle : rectangles) {
                g2d.drawRect( rectangle.x, rectangle.y, rectangle.w, rectangle.h);
            }

        }
    };


    public DrawingArea(final Rectangle[] rectangles, RectangleOrientation orientation) {
        this.rectangles = rectangles;
        this.orientation = orientation;
        setMaximumSize(new Dimension(500,300));
        setMinimumSize(new Dimension(500,300));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLUE),
                BorderFactory.createLineBorder(Color.WHITE, 5)));
        add(paintArea, BorderLayout.CENTER);
    }

    public void setRectangles(Rectangle[] rectangles) {
        this.rectangles = rectangles;
        paintArea.repaint();
    }

}
