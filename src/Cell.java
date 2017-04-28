/**
 * a cell is a wrapper of Coord and it contains information of
 *  coord,
 *  startToCurrCost value,
 *  currToEndEstimate value
 *  and a counter that records the order when adding the cell into potentialNexts
 */
public class Cell{
	Coord coord;
	Cell parent;
	int startToHereCost;
	int hereToEndEstimate;
	int insertOrder;

	Cell(Coord coord)
	{
		this.coord = coord;
	}

	public void setParent(Cell parentCell)
	{
		this.parent = parentCell;
	}

	public void setStartToHereCost(int v)
	{
		this.startToHereCost = v;
	}

	public void setHereToEndEstimate(int v)
	{
		this.hereToEndEstimate = v;
	}

	public void setInsertOrder(int v)
	{
		this.insertOrder = v;
	}

	public int getPathCostEstimate()
	{
		return startToHereCost + hereToEndEstimate;
	}
}