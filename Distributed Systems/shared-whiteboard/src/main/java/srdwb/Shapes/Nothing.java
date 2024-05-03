package srdwb.Shapes;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;


public class Nothing extends Shape{
    	public Nothing() {
    		
    	}

        @Override
        public void draw(JPanel panel, MouseEvent e, Color colour, double size) {

        }

        @Override
        public void dragDraw(JPanel panel, MouseEvent e, Color colour, double size) {

        }
 
        @Override
        public void finalise(JPanel panel, MouseEvent e, Color colour) {

        }
        
        @Override
        public Shape refresh() {
            return new Nothing();
        }

}
