package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Shapes to be drawn on whiteboard
 */
public abstract class Shape {
	private static final long serialVersionUID = 1L;
    public boolean finished = false;
    public int width;
    public int height;
    public int x;
    public int y;
    public int drawWidth;
    public int drawHeight;
    public int colour;
    public double size;

    public Shape() {}

    /**
     * Drawing at point
     * @param panel: to draw on
     * @param e: event details
     * @param colour: aesthetics
     * @param size: aesthetics
     */
    public abstract void draw(JPanel panel, MouseEvent e, Color colour, double size);

    /**
     * Drawing across points (dragging mouse)
     * @param panel: drawing onto
     * @param e: event details
     * @param colour: aesthetics
     * @param size: aesthetics
     */
    public abstract void dragDraw(JPanel panel, MouseEvent e, Color colour, double size);

    /**
     * Any finishes to drawing before commit
     * @param contentPane: drawing onto
     * @param e: event details
     * @param currentColour: aesthetics
     */
    public abstract void finalise(JPanel contentPane, MouseEvent e, Color currentColour);

    /**
     * Generates new shape to replace
     * @return Shape: renewed version
     */
    public abstract Shape refresh();

    /**
     * Draws preexisting shape
     * @param g: to draw on
     */
    public void drawGraphics(Graphics g) {
    }
}

