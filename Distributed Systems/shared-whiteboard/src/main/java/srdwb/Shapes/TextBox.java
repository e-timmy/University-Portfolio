package srdwb.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TextBox extends Shape {
	private static final long serialVersionUID = 1L;

    public String text;

    public TextBox() {text = "";}

    /**
     * Gathers user input for string drawing
     * @param panel
     * @param e
     * @param colour
     * @param size
     */
    @Override
    public void draw(JPanel panel, MouseEvent e, Color colour, double size) {
        System.out.println("Drawing Textbox");

        // Style
        x = e.getX();
        y = e.getY();
        this.colour = colour.getRGB();
        this.size = (int)size*3;

        // Prompt
        JTextArea textArea = new JTextArea();
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        int ret = JOptionPane.showConfirmDialog(panel, new JScrollPane(textArea), "Text", JOptionPane.OK_OPTION);
        if (ret == 0) {
            text = textArea.getText();
            drawString(panel);
        }

    }

    /**
     * Paints string to board
     * @param panel
     */
    private void drawString(JPanel panel) {
        // String tokens
        String[] lines = text.split("\n");
        Graphics g = panel.getGraphics();

        // String aesthetics
        g.setFont(new Font("Times Roman", Font.PLAIN, (int) this.size));
        g.setColor(new Color(colour));

        // Height and width to centre
        int height = g.getFontMetrics().getHeight();
        int new_y = y - (lines.length/2)*height;
        int width;
        for (String string:lines) {
            width = g.getFontMetrics().stringWidth(string);
            g.drawString(string, x - width/2, new_y);
            new_y += height;
        }
        finished = true;
    }

    @Override
    public void drawGraphics(Graphics g) {
        // String aesthetics
        String[] lines = text.split("\n");
        g.setFont(new Font("Calibri", Font.PLAIN, (int) this.size));
        g.setColor(new Color(colour));

        // Height and width to centre
        int height = g.getFontMetrics().getHeight();
        int new_y = y - (lines.length/2)*height;
        int width;
        for (String string:lines) {
            width = g.getFontMetrics().stringWidth(string);
            g.drawString(string, x - width/2, new_y);
            new_y += height;
        }
        finished = true;
    }


    @Override
    public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {
    }

    @Override
    public void finalise(JPanel panel, MouseEvent e, Color currentColour) {
    }

    @Override
    public Shape refresh() {
        return new TextBox();
    }


}
