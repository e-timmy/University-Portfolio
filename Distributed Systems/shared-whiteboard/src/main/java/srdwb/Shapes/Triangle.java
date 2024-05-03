package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Triangle extends Shape {
	private static final long serialVersionUID = 1L;

    public Triangle() {
    	width = 10;
        height = 10;
    }

    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {
        System.out.println("Drawing Triangle");

        drawWidth = (int) (width* size);
        drawHeight = (int) (height * size);

        x = e.getX();
        y = e.getY();
        this.colour = colour.getRGB();
        this.size = (int) size;

        Polygon p = new Polygon();
        p.addPoint(x-drawWidth/2, y+drawHeight/2);
        p.addPoint(x+drawWidth/2, y+drawHeight/2);
        p.addPoint(x, y-drawHeight/2);

        Graphics g = panel.getGraphics();
        g.setColor(colour);
        g.fillPolygon(p);
        finished = true;
    }

    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {

        //draw(panel, e, colour, size);
    }

    @Override
    public void finalise(JPanel contentPane, MouseEvent e, Color currentColour) {

    }

    @Override
    public Shape refresh() {
        return new Triangle();
    }

    @Override
    public void drawGraphics(Graphics g) {
        Polygon p = new Polygon();
        p.addPoint(x-drawWidth/2, y+drawHeight/2);
        p.addPoint(x+drawWidth/2, y+drawHeight/2);
        p.addPoint(x, y-drawHeight/2);

        g.setColor(new Color(colour));
        g.fillPolygon(p);
    }
}