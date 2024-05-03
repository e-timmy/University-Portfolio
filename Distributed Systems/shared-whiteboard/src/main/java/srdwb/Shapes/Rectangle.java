package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Rectangle extends Shape {
	private static final long serialVersionUID = 1L;
    
    public Rectangle() {
    	width = 10;
        height = 4;
    }

    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {

        System.out.println("Drawing Rectangle");

        drawWidth = (int) (width* size);
        drawHeight = (int) (height * size);

        x = e.getX() - drawWidth/2;
        y = e.getY() - drawHeight/2;
        this.colour = colour.getRGB();
        this.size = size;

        // Could call drawUpdate for this??
        Graphics g = panel.getGraphics();
        g.setColor(colour);
        g.fillRect(x, y, drawWidth, drawHeight);
        finished = true;
    }

    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {}

    @Override
    public void finalise(JPanel contentPane, MouseEvent e, Color currentColour) {}

    @Override
    public Shape refresh() {
        return new Rectangle();
    }

    @Override
    public void drawGraphics(Graphics g) {
        g.setColor(new Color(colour));
        g.fillRect(x, y, drawWidth, drawHeight);
    }
}
