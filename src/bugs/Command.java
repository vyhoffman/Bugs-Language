package bugs;

import java.awt.Color;

/**Class representing a line for the View to draw.
 * @author Nicki Hoffman
 */
public class Command {
	Color color;
	double x1, y1, x2, y2;
	
	/**Constructor for a Command object
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color java.awt.Color object
	 */
	Command(double x1, double y1, double x2, double y2, Color color) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
	}
}
