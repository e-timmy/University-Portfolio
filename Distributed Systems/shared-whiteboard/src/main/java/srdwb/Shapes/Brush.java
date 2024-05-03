package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;

public class Brush extends Shape {
	private static final long serialVersionUID = 1L;
    private Point previousPoint;
    private Point currentPoint;
    public ArrayList<Point> points;
    
    public Brush() {
        points = new ArrayList<>();
        width = 3;
    }

    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {}

    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {

        // Retrieve and set graphics
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;

        // Set beginning
        if (previousPoint == null) {
            // Define shape
            previousPoint = new Point(e.getX(), e.getY());
            points.add(previousPoint);
            this.colour = colour.getRGB();
            drawWidth = (int) (width*size);

        } else {
            currentPoint = new Point(e.getX(), e.getY());
            points.add(currentPoint);
            // Draw part of freehand
            g2.setColor(colour);
            g2.setStroke(new BasicStroke(drawWidth, CAP_ROUND, JOIN_ROUND));
            g2.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
            previousPoint = currentPoint;
        }
    }

    @Override
    public void drawGraphics(Graphics g) {
        g.setColor(new Color(colour));
        Graphics2D g2 = (Graphics2D) g;

        previousPoint = null;
        for (Point point : points) {

            if (previousPoint == null) {
                previousPoint = point;
            } else {
                currentPoint = point;
                g2.setStroke(new BasicStroke(drawWidth, CAP_ROUND, JOIN_ROUND));
                g2.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
                previousPoint = currentPoint;
            }
        }
    }

    @Override
    public void finalise(JPanel contentPane, MouseEvent e, Color currentColour) {
        finished = true;
    }

    @Override
    public Shape refresh() {
        return new Brush();
    }
}
