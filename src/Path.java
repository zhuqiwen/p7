import java.util.LinkedList;

/**
 * A Path represents a sequence of connected coordinates starting 
 * with wire.from and (presumably) ending with wire.to.
 */

public class Path extends LinkedList<Coord> {
  protected Wire wire;
  
  public Path(Wire wire) {
    this.wire = wire;
    add(wire.from);
  }
  
  /**
   * Returns the length of this path.
   */
  public int length() {
    return size();
  }
  
}
