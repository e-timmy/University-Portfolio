package srdwb.clientGUI;

import srdwb.Shapes.Shape;
import srdwb.client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Whiteboard for drawing on
 */
public class WhiteBoard extends JPanel {

    private Client client;

    private BufferedImage canvasBuffer = null;
    
    private Color bgColor = new Color(245,245,245); // white smoke
    
    public WhiteBoard(Client client) {
        super();
        this.client = client;
    }

	/****************************************************************************************/


	/**
	 * Paint image
	 */
	public void paintImmediately() {
    	super.invalidate();
    	BufferedImage buffer = getCanvasBuffer();
    	if (buffer != null) {
    		super.getGraphics().drawImage(buffer,0,0,buffer.getWidth(), buffer.getHeight(),null);
    	}
    }

	/**
	 * Pains board
	 * @param g  the <code>Graphics</code> context in which to paint
	 */
	@Override
    public void paint(Graphics g) {
    	BufferedImage canvasBuffer = getCanvasBuffer();
    	if (canvasBuffer != null) {
    		g.drawImage(canvasBuffer, 0, 0, canvasBuffer.getWidth(), canvasBuffer.getHeight(), null);
    	}else {
    		super.paint(g);
    	}
    }

	/**
	 * Paints board
	 * @param g the <code>Graphics</code> object to protect
	 */
    @Override
    public void paintComponent(Graphics g) {
    	BufferedImage canvasBuffer = getCanvasBuffer();
    	if (canvasBuffer != null) {
    		g.drawImage(canvasBuffer, 0, 0, canvasBuffer.getWidth(), canvasBuffer.getHeight(), null);
			synchroniseBoard();
    	}else {
    		super.paintComponent(g);
    	}
    }

	/**
	 * Synchronise board based on shapes
	 */
    synchronized public void synchroniseBoard() {
        System.out.println("Synchronising board");
        ArrayList<Shape> state = client.getBoardState();
        BufferedImage buffer = getCanvasBuffer();
        clearCanvas();
        Graphics g = buffer == null? super.getGraphics() : buffer.getGraphics();
        for (Shape shape: state) {
            shape.drawGraphics(g);
        }
        paintImmediately();
    }

	/**
	 * Wipe canvas
	 */
	public void clearCanvas() {
		Graphics g = this.getGraphics();
		g.setColor(bgColor);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		paintImmediately();
	}

	/****************************************************************************************/

	/**
	 * Retrieve graphics of canvas
	 * @return
	 */
	@Override
	public Graphics getGraphics() {
		BufferedImage buffer = getCanvasBuffer();
		return buffer == null? super.getGraphics() : buffer.getGraphics();
	}

	/**
	 * Snapshot of canvas
	 * @return BufferedImage
	 */
	public BufferedImage getCanvasSnapshot() {
    	BufferedImage current = getCanvasBuffer();
    	if (current == null) {
    		return null;
    	}
    	BufferedImage snapshot = new BufferedImage(current.getWidth(), current.getHeight(), current.getType());
    	snapshot.setData(current.getData());
    	return snapshot;
    }
    
    /**
     * Get buffer
     * @return canvasBuffer
     */
    private BufferedImage getCanvasBuffer() {
    	if (canvasBuffer == null) {
    		if (this.getWidth() != 0 && this.getHeight() != 0) {
    			canvasBuffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    		}
    	}
    	return canvasBuffer;
    }

	/**
	 * Set buffer size according to dimension <br />
	 * if width or height is equal to 0 fail silently
	 * @param dimension
	 */
	public void setBufferSize(Dimension dimension) {
		if (dimension.width == 0 || dimension.height == 0) {
			return;
		}
		BufferedImage buffer = getCanvasBuffer();
		if (buffer != null) {
			int bw = buffer.getWidth();
			int bh = buffer.getHeight();
			int dw = dimension.width;
			int dh = dimension.height;
			int nw = bw < dw ? bw : dw;
			int nh = bh < dh ? bh : dh;
			if (dw > 0 && dh > 0 && (dw != bw || dh != bh)) {
				BufferedImage newBuffer = new BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR);
				newBuffer.setData(buffer.getData(new Rectangle(0, 0, nw, nh)));
				buffer = newBuffer;
			}
		}
	}

	/**
	 * set canvas buffer refill color
	 */
	public void setBgColor(Color color) {
		this.bgColor = color;
	}
}

/****************************************************************************************/
