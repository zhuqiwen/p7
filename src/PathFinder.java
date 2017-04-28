import java.io.File;
import java.util.*;

/**
 * TD
 *
 * Most of the work for this project involves implementing the
 * connectAllWires() method in this class. Feel free to create
 * any helper methods that you deem necessary.
 *
 * Your goal is to come up with an efficient algorithm that will
 * find a layout that connects all the wires (if one exists) while
 * attempting to minimize the overall wire length.
 */

public class PathFinder {


	/**
	 * the potentialNexts needs a CUSTOM COMPARATOR that
	 *  1st, compare the pathCostEstimate value (startToCurrCost + currToEndEstimate) to push the cell with minimum pathCostEstimate to the top.
	 *  2nd, if two cell has the same pathCostEstimate, then compare their insertOrder, the one insterted later comes first; this is to prevent path finder
	 *      from going back.
	 * @return Queue
	 */
	public static Queue<Cell> produceQueue()
	{
		Comparator<Cell> customComparator = new Comparator<Cell>() {
			@Override
			public int compare(Cell c1, Cell c2)
			{
				if(c1.getPathCostEstimate() == c2.getPathCostEstimate())
					return c2.insertOrder - c1.insertOrder;
				return c1.getPathCostEstimate() - c2.getPathCostEstimate();
			}
		};
		Queue<Cell> potentialNexts = new PriorityQueue<>(Constants.MAX_BOARD_SIZE, customComparator);

		return potentialNexts;
	}


	/**
	 * for now, use priority queue provided by Java temporarily.
	 * will need to define update() for potentialNexts, to improve priority quere's performance.

	 */


	/**
	 * a helper that determines if a Cell / Coord is:
	 *  1, on obard,
	 *  2, NOT a obstacle,
	 *  3, NOT occupied by an existing path that connects a wire's two endpoints
	 * @param Cell
	 * @return boolean
	 */
	private static boolean canBeAddedToPotentialNexts(Cell neighborCell, Chip chip, Map<Coord, Cell> visitedCells, Map<Coord, Integer> onPath)
	{

		Coord neighborCoord = neighborCell.coord;

		//if coord is a from or a to, or if a coord is on a found path
		if(onPath.containsKey(neighborCoord))
			return false;

		// if coord has been added into visited list.
		if(visitedCells.containsKey(neighborCoord))
			return false;

		// if coord is an obstacle
		if(chip.grid.get(neighborCoord) == Constants.OBSTACLE)
			return false;
//
		// if coord not on chip.
		if(!neighborCoord.onBoard(chip.dim))
			return false;

//		return chip.grid.get(neighborCoord) == Constants.FREE;
		if(chip.grid.get(neighborCoord) == 0)
			return true;
		return true;
	}


	/**
	 * a helper that sets up attributes for neighbor cells of the current cell.
	 * for a legal neighbor that can be added into the next potential cell list,
	 * if the neighbor is already in the list,
	 *  see if current cell's startToHereCost + 1 > neighbor's startToHereCost
	 *      if so, update neighbor's startToHereCost to be current cell's startToHereCost + 1
	 *
	 * if the neighbor is NOT in the queue,
	 *  calculate its attributes and add it into the queue.
	 * @param Cell
	 * @param Queue
	 * @param Wire
	 * @return int
	 */
	private static int setupNeighbors(
			Cell currCell,
			Queue<Cell> potentialNexts,
			Wire wire,
			int insertCounter,
			Chip chip,
			Map<Coord, Cell> locker,
			Map<Coord, Cell> visitedCells,
			Map<Coord, Integer> onPath
	)
	{
		for(Coord neighbor: chip.neighbors(currCell.coord))
		{
			Cell neighborCell = new Cell(neighbor);
			if(canBeAddedToPotentialNexts(neighborCell, chip, visitedCells, onPath))
			{
//				if(potentialNexts.contains(neighborCell))
				if(locker.containsKey(neighbor))
				{
					neighborCell = locker.get(neighbor);
					if(currCell.startToHereCost + 1 > neighborCell.startToHereCost)
					{
						potentialNexts.remove(neighborCell);
						//update insert order, parent, and startToCurrentCost value
						insertCounter++;
						neighborCell.setInsertOrder(insertCounter);
						neighborCell.setParent(currCell);
						neighborCell.setStartToHereCost(currCell.startToHereCost + 1);
						//re-insert to make sure the latest minimum be on the top
						potentialNexts.add(neighborCell);
					}
				}
				else
				{
					insertCounter++;
					neighborCell.setInsertOrder(insertCounter);
					// parent is used to trace back path
					neighborCell.setParent(currCell);
					//to indicate a potential direction for exploration.
					int toEndEstimate = manhattanDistance(neighborCell.coord, wire.to);
					neighborCell.setHereToEndEstimate(toEndEstimate);
					neighborCell.setStartToHereCost(currCell.startToHereCost + 1);
					potentialNexts.add(neighborCell);
					locker.put(neighbor, neighborCell);
				}
			}
		}

		return insertCounter;
	}


	/**
	 * a helper to give Manhattan distance between two coords
	 * this is used to guide the path finder to the desired derection.
	 * @param Coord
	 * @param Coord
	 * @return int
	 */
	private static int manhattanDistance(Coord coord1, Coord coord2)
	{
		return  Math.abs(coord1.getX() - coord2.getX()) + Math.abs(coord1.getY() - coord2.getY());
	}


	/**
	 * a helper to produce a path that connects a wire's two endpoints.
	 * it first trace back from the end to the start using each cell's parent information,
	 *  putting each cell's coord in a list in reverse order.
	 * then, walk through reversely, adding each coord to the Path object.
	 * @param visitedCells
	 * @param wire
	 * @param path
	 * @return
	 */
	private static Path drawPath(Map<Coord, Cell> visitedCells, Wire wire, Chip chip, Map<Coord, Integer> onPath)
	{
		Path path = new Path(wire);

		List<Coord> traceBack = new ArrayList<>();
		Cell end = visitedCells.get(wire.to);
		traceBack.add(end.coord);
		while(end.parent != null)
		{
			end = end.parent;
			traceBack.add(end.coord);
		}

		for(int i = traceBack.size() - 1 - 1; i >= 0; i--)
		{
			Coord coord = traceBack.get(i);
			path.add(coord);
			onPath.put(coord, wire.wireId);
			chip.mark(coord, wire.wireId);
		}

		return path;
	}


	/**
	 * the path finder.
	 * algorithm:
	 *
	 * each cell is assigned a path estimate value, the sum of real cost from start to the cell and estimated cost from the cell to the end.
	 *  1. the smaller the path estimate is, the closer it is to the end. This is because the estimated cost from the cell to the end helps to guide the direction.
	 *  2. Manhattan distance is used for the estimated cost from the cell to the end.
	 *
	 * for a for each current cell, we set up these attributes of its legal neighbors*, and add these neighbors into a priority queue, called potentialNexts.
	 * as long as the potentialNexts is not empty, we set current cell to be the cell with minimum path estimate value in the potentialNexts,
	 *  set up the neighbors of the new current cell, and put current cell into a temporary visited list.
	 *  *1. a legal neighbor needs to be:
	 *      1. on the board
	 *      2. NOT an obstacle
	 *      3. NOT used by any path
	 *
	 * if the end cell is found in the visited list, meaning a path from start to end is found, then we trace back the path.
	 * or if the potentialNexts is empty, meaning we have tried every possibility and no path is found, we return null.
	 *
	 * NOTE: if multiple cells in the potentialNexts share the same minimum path estimate value, the last interted one should be pop out.
	 *       to make sure this, a custom comparator is used for the potentialNexts.
	 * @param Wire
	 * @return Path
	 */
	private static Path findPath(Wire wire, Queue<Cell> potentialNexts, Chip chip, Map<Coord, Integer> onPath)
	{
		Map<Coord, Cell> visitedCells = new HashMap<>();
		Map<Coord, Cell> locker = new HashMap<>();
		int insertCounter = 0;
		Cell start = new Cell(wire.from);
		start.setStartToHereCost(0);
		Cell end = new Cell(wire.to);
		Cell currCell;

		potentialNexts.clear();
		potentialNexts.add(start);
		locker.put(start.coord, start);
		while (!potentialNexts.isEmpty())
		{
//			System.out.println("\t[in findPath]");
//			System.out.println("\t\tgoing to jump to: " + potentialNexts.peek().coord.toString());
			currCell = potentialNexts.poll();
			locker.remove(currCell.coord);

			visitedCells.put(currCell.coord, currCell);
			insertCounter = setupNeighbors(currCell, potentialNexts, wire, insertCounter, chip, locker, visitedCells, onPath);

			if(visitedCells.containsKey(end.coord))
			{

				return drawPath(visitedCells, wire, chip, onPath);
			}
		}

		return null;

	}


	/**
	 * TD
	 *
	 * Lays out a path connecting each wire on the chip, and then
	 * returns a map that associates a wire id numbers to the paths
	 * corresponding to the connected wires on the grid. If it is
	 * not possible to connect the endpoints of a wire, then there
	 * should be no association for the wire id# in the result.
	 */
	public static Map<Integer, Path> connectAllWires(Chip chip)
	{
		Map<Integer, Path> allWires = new HashMap<>();
		Queue<Cell> potentialNexts = produceQueue();

		// a global path recorder to store <coord, wireID> that help to decide if a coord can be used as next step
		// for a chip, first put all wires's froms and tos into this path recorder
		// then, for each wire, remove its from and to, do the path recording while searching path, and after searching path,
		// 		re-add the wire's from and to to the recorder.
		Map<Coord, Integer> onPath = new HashMap<>();
		for(Wire wire: chip.wires)
		{
			onPath.put(wire.from, wire.wireId);
			onPath.put(wire.to, wire.wireId);
		}



		for(Wire wire: chip.wires)
		{
			onPath.remove(wire.from);
			onPath.remove(wire.to);

			System.out.println("working on wire " + wire.wireId + ", from " + wire.from.toString() + " to " + wire.to.toString());
			Path path = findPath(wire, potentialNexts, chip, onPath);

			if (path != null)
			{
				allWires.put(wire.wireId, path);
				System.out.println("found a path for wire " + wire.wireId);
				System.out.println(chip.toString());
			}
			else
			{
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("there is no path for wire " + wire.wireId);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@ ");
			}

			onPath.put(wire.from, wire.wireId);
			onPath.put(wire.to, wire.wireId);

		}

		if(allWires.size() != chip.wires.size())
		{
			if(allWires.size() == 0)
			{
				System.out.println("This chip is unsolvable");
			}
			System.out.println("This chip has only a partial solution");
		}
		else
		{
			System.out.println("This chip is fully solvable");
		}
		return allWires;
	}


	private static Map<Integer, Path> connectAllWiresNoPrompts(Chip chip)
	{
		Map<Integer, Path> allWires = new HashMap<>();
		Queue<Cell> potentialNexts = produceQueue();
		Map<Coord, Integer> onPath = new HashMap<>();
		for(Wire wire: chip.wires)
		{
			onPath.put(wire.from, wire.wireId);
			onPath.put(wire.to, wire.wireId);
		}

		for(Wire wire: chip.wires)
		{
			onPath.remove(wire.from);
			onPath.remove(wire.to);

			Path path = findPath(wire, potentialNexts, chip, onPath);

			if (path != null)
			{
				allWires.put(wire.wireId, path);
			}

			onPath.put(wire.from, wire.wireId);
			onPath.put(wire.to, wire.wireId);

		}

		return allWires;
	}


	public static void successAndFailure(Chip chip, String filename)
	{
		Map<Integer, Path> allWires = connectAllWiresNoPrompts(chip);
		int successCount = 0;
		List<Integer> successList = new ArrayList<>();
		List<Integer> failureList = new ArrayList<>();

		for(Wire wire: chip.wires)
		{
			if(allWires.containsKey(wire.wireId))
			{
				successCount++;
				successList.add(wire.wireId);
			}
			else
			{
				failureList.add(wire.wireId);
			}
		}

		int failureCount = chip.wires.size() - successCount;

		int numbOfWireIdLines = successCount >= failureCount ? successCount : failureCount;
		int numOfCharPerLine = 50;
		int heighOfRow = 3;

		printHead(filename, chip.wires.size());
		printUpBorder(numOfCharPerLine);
		printTableHead("# of success", "# of failure", heighOfRow, numOfCharPerLine);
		printTableHead(""+successCount, ""+failureCount, heighOfRow, numOfCharPerLine);
		printWireIDColumn(successList, failureList, numbOfWireIdLines, numOfCharPerLine, 1);
		printUpBorder(numOfCharPerLine);
	}

	private static void printHead(String filename, int numOfWires)
	{
		String string = " "+filename + " has " + numOfWires + " wires ";
		for(int j = 0; j < 2; j++)
		{
			if(j == 0)
			{
				for(int i = 0; i < string.length() + 2; i++)
				{
					System.out.print("*");
				}

			}
			else
			{
				System.out.print("*");
				System.out.print(string);
				System.out.print("*");
			}
			System.out.println();
		}

	}

	private static void printUpBorder(int numChar)
	{
		while (numChar > 0)
		{
			System.out.print("*");
			numChar--;
		}
		System.out.println();
	}

	private static void printTableHead(String s1, String s2, int height, int numCharOfOneLine)
	{
		for(int h = 0; h < height; h++)
		{
			// ugly hardcode for now
			if(h == height/2)
			{
				printTextLine(s1, s2, numCharOfOneLine);
			}
			else
			{
				for(int i = 0; i < numCharOfOneLine; i++)
				{
					if(i == 0 || i == numCharOfOneLine / 2 || i  == numCharOfOneLine - 1)
					{
						System.out.print("*");
					}
					else
					{
						System.out.print(" ");
					}
				}
				System.out.println();
			}
		}
		printUpBorder(numCharOfOneLine);
	}

	private static void printTextLine(String s1, String s2, int numChar)
	{
		String string1 = s1;
		String string2 = s2;

		for(int i = 0; i < numChar; i++)
		{
			if (i == 0 || i == numChar / 2 || i == numChar - 1)
			{
				System.out.print("*");
			}
			else
			{
				if(i == (numChar / 2 - string1.length()) / 2)
				{
					System.out.print(string1);
					i = i + string1.length() - 1;
				}
				else if(i == numChar/2 + (numChar / 2 - string1.length()) / 2)
				{
					System.out.print(string2);
					i = i + string2.length() - 1;
				}
				else
				{
					System.out.print(" ");
				}
			}
		}
		System.out.println();
	}

	private static void printWireIDColumn(List<Integer> IDs1, List<Integer> IDs2, int numOfWireIDs, int numCharOfOneLine, int marginTop)
	{
		while (marginTop > 0)
		{
			marginTop--;
			printEmptyLine(numCharOfOneLine);
		}

		for(int j = 0; j < numOfWireIDs; j ++)
		{
			String sucWireID, failWireID;
			if(j < IDs1.size())
			{
				sucWireID = "Wire " + IDs1.get(j).toString();
			}
			else
			{
				sucWireID = "NULL";
			}

			if(j < IDs2.size())
			{
				failWireID = "Wire " + IDs2.get(j).toString();
			}
			else
			{
				failWireID = "NULL";
			}

			printTextLine(sucWireID, failWireID, numCharOfOneLine);

		}
	}

	private static void printEmptyLine(int numChar)
	{
		for(int i = 0; i < numChar; i++)
		{
			if(i == 0 || i == numChar / 2 || i  == numChar - 1)
			{
				System.out.print("*");
			}
			else
			{
				System.out.print(" ");
			}
		}
		System.out.println();
	}



	/**
	 * TD
	 *
	 * Returns the sum of the lengths of all non-null paths in the given layout.
	 */
	public static int totalWireUsage(Map<Integer, Path> layout)
	{
		// the sum of length of each exist path.
		int sum = 0;
		for(Integer key : layout.keySet())
		{
			sum += layout.get(key).length();
		}

		return sum;
	}


	public static void testOn(String prefix)
	{
		Chip chip;
		Map<Integer, Path> layout;


		File inputFiles = new File("inputs/");
		File[] fileList = inputFiles.listFiles();
		for(int i = 0; i < fileList.length; i++)
		{
			if(fileList[i].isFile() && fileList[i].getName().startsWith(prefix))
			{
				System.out.println(fileList[i].getPath().toString());
				chip = new Chip(new File(fileList[i].getPath().toString()));
				System.out.println("before searching for paths:");
				System.out.println(chip.toString());

				layout = PathFinder.connectAllWires(chip);
				System.out.println("after searching for paths");
				System.out.println("there are " + layout.size() + " paths");

				System.out.println(chip.toString());

			}
		}
	}

	public static void produceSummaries(String prefix)
	{
		Chip chip;
		File inputFiles = new File("inputs/");
		File[] fileList = inputFiles.listFiles();
		for(int i = 0; i < fileList.length; i++)
		{
			if(fileList[i].isFile() && fileList[i].getName().startsWith(prefix))
			{
				String filename = fileList[i].getName();
				chip = new Chip(new File(fileList[i].getPath().toString()));
				successAndFailure(chip,filename);
				System.out.println();
			}

		}
	}

	public static void main(String[] args)
	{
		String[] prefixs = {"small", "medium", "big", "huge"};
////		String[] prefixs = {"small"};
//
//		for(String prefix: prefixs)
//		{
//			testOn(prefix);
//		}


//		String file = "small_06";
//		Chip chip = new Chip(new File("inputs/" + file + ".in"));

		for(String prefix: prefixs)
		{
			produceSummaries(prefix);
		}

	}
}

