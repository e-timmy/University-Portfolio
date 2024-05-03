package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Line extends Shape {
	//private static final long serialVersionUID = 1L;

    public Point p1;
    public Point p2;
    
    public Line() {
    	width = 1;
        p1 = null;
        p2 = null;
    }

    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {
        System.out.println("Drawing Line");

        if (p1 != null) {
            p2 = new Point(e.getX(), e.getY());
            Graphics g = panel.getGraphics();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(colour);
            g2.setStroke(new BasicStroke(drawWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            finished = true;
        } else {
            p1 = new Point(e.getX(), e.getY());
            this.colour = colour.getRGB();
            this.drawWidth = (int) (width*size);
            System.out.println("Have point");
        }
    }

    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {

    }

    @Override
    public void finalise(JPanel panel, MouseEvent e, Color colour) {

    }

    @Override
    public Shape refresh() {
        System.out.println(p1);
        if (p2 != null) {
            System.out.println("New Line Created");
            return new Line();
        }
        return this;
    }

    @Override
    public void drawGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(colour));
        g2.setStroke(new BasicStroke(drawWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

}
