/**
 * This is a simple structure to encapsulate an obstacle on a chip.
 * An obstacle is a rectangle defined by its upper left corner and
 * its lower right corner.
 *
 * TD: Implement contains().
 */

public class Obstacle {
	protected Coord upperLeft, lowerRight;

	/**
	 * Creates a new obstacle covering the rectangle with the given upper
	 * left corner and lower right corner on a grid.
	 */
	public Obstacle(Coord upperLeft, Coord lowerRight) {
		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
	}

	/**
	 * Creates a new obstacle whose upper left corner is (x1, y1) and whose
	 * lower right corner is (x2, y2).
	 */
	public Obstacle(int x1, int y1, int x2, int y2) {
		this(new Coord(x1, y1), new Coord(x2, y2));
	}

	/**
	 * TD
	 *
	 * Returns true iff the given coordinate is contained in the region of cells
	 * covered by this obstacle.
	 */
	public boolean contains(Coord coord)
	{
		return coord.getX() <= lowerRight.getX() && coord.getX() >= upperLeft.getX()
				&& coord.getY() <= lowerRight.getY() && coord.getY() >= upperLeft.getY();
	}

	/**
	 * Returns a textual representation of this obstacle.
	 */
	public String toString() {
		return String.format("Obstacle[%s,%s]", upperLeft, lowerRight);
	}
}
