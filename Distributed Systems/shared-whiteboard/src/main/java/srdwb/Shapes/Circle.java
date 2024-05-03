package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Circle extends Shape {
	private static final long serialVersionUID = 1L;
    
    public Circle() {
    	width = 10;
        height = 10;
    }

    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {
        System.out.println("Drawing Circle");

        // Dimensions
        drawWidth = (int) (width* size);
        drawHeight = (int) (height * size);
        x = e.getX() - drawWidth/2;
        y = e.getY() - drawHeight/2;
        this.colour = colour.getRGB();
        this.size = size;

        Graphics g = panel.getGraphics();
        g.setColor(colour);
        g.fillOval(x, y, drawWidth, drawHeight);
        finished = true;
    }

    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {
        //draw(panel, e, colour, size);
    }

    @Override
    public void drawGraphics(Graphics g) {
        g.setColor(new Color(colour));
        g.fillOval(x, y, drawWidth, drawHeight);
    }

    @Override
    public void finalise(JPanel contentPane, MouseEvent e, Color currentColour) {

    }

    @Override
    public Shape refresh() {
        return new Circle();
    }

}
