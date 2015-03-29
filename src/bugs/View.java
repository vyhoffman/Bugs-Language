package bugs;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class View extends JPanel {
	static final long serialVersionUID = 1L;
	Interpreter in;
	
	View() { super(); }
	
	View (Interpreter in) {
		super();
		this.in = in;
		this.setSize(500,500);
		this.setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		if (in != null) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			for (Command c : in.lines) paint(g, c);
			for (String b : in.bugs.keySet()) paint(g, in.bugs.get(b));			
		}

	}
	
	public void paint(Graphics g, Command c) {
		if (c.color == null) return;
		g.setColor(c.color);
		int x1 = (int) scaleX(c.x1);
		int y1 = (int) scaleX(c.y1);
		int x2 = (int) scaleX(c.x2);
		int y2 = (int) scaleX(c.y2);
		g.drawLine(x1, y1, x2, y2);
	}
	
	/**
	 * Paints a triangle to represent this Bug.
	 * 
	 * @param g Where to paint this Bug.
	 */
	public void paint(Graphics g, Bug b) {
	    if (b.color == null) return;
	    g.setColor(b.color);
	    
	    int x1 = (int) (scaleX(b.x) + computeDeltaX(12, (int)b.angle));
	    int x2 = (int) (scaleX(b.x) + computeDeltaX(6, (int)b.angle - 135));
	    int x3 = (int) (scaleX(b.x) + computeDeltaX(6, (int)b.angle + 135));
	    
	    int y1 = (int) (scaleY(b.y) + computeDeltaY(12, (int)b.angle));
	    int y2 = (int) (scaleY(b.y) + computeDeltaY(6, (int)b.angle - 135));
	    int y3 = (int) (scaleY(b.y) + computeDeltaY(6, (int)b.angle + 135));
	    g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
	}
	
	double scaleX(double x) {
		return x * this.getWidth() / 100.0;
	}
	
	double scaleY(double y){
		return y * this.getHeight() / 100.0;
	}

	/**
	 * Computes how much to move to add to this Bug's x-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees".
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the x-coordinate.
	 */
	private static double computeDeltaX(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.cos(radians);
	}

	/**
	 * Computes how much to move to add to this Bug's y-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees.
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the y-coordinate.
	 */
	private static double computeDeltaY(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.sin(-radians);
	}
	
	@Override
	public void update(Graphics g) {
		// TODO Auto-generated method stub
		repaint();
		
	}
}
