import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.swing.*;

/**
 * There's nothing for you to do here.
 */

public class GUI extends JFrame {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			ToolTipManager.sharedInstance().setInitialDelay(0);
			ToolTipManager.sharedInstance().setDismissDelay(15000);
			UIManager.put("ToolTip.background", Constants.PATH_COLOR);
			UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder());
//			UIManager.put("MenuItem.foreground", Constants.MENU_COLOR);
//			UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private JCheckBox showPath;
//	private JLabel xLabel, yLabel;
	private JLabel totalWireUsage, isSolvable;
	private JLabel[][] cells;
//	private SequenceAligner strands;
	private Chip chip;
	private String filename;

	public GUI(Chip chip, String filename) {
		setTitle(Constants.TITLE);
		this.chip = chip;
		this.filename = filename;

		// Pad x and y with blanks on left to synchronize indices on the grid
//		String x = "  " + strands.getX(), y = "  " + strands.getY();
		int numRows = (int) chip.dim.getHeight(); // Rows are labeled with chars in x
		int numCols = (int) chip.dim.getWidth(); // Cols are labeled with chars in y


		JPanel main = new JPanel(new BorderLayout());
		JPanel grid = new JPanel(new GridLayout(numRows, numCols, 2, 2));
		JPanel controls = new JPanel(new GridLayout(0, 2));
		JPanel controlsLeft = new JPanel(new GridLayout(2, 0));



		grid.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		// Set up the look and feel
		Font charFont = new Font("Courier", Font.BOLD, 20);
		Font scoreFont = new Font("Ariel", Font.PLAIN, 10);

		cells = new JLabel[numRows][numCols];

		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				Coord coord = new Coord(col, row);
				Integer coordWireID = chip.grid.get(coord);

				JLabel cell = new JLabel() {
					@Override
					public Point getToolTipLocation(MouseEvent event) {
						return new Point(0, 0);
					}
				};
				cells[row][col] = cell;
				cell.setOpaque(true);
				cell.setFont(scoreFont);

				if(coordWireID == Constants.OBSTACLE)
				{
					cell.setBackground(Constants.OBSTACLE_BACKGROUND_COLOR);
					cell.setText(Constants.OBSTACLE_FACE);
					cell.setForeground(Constants.OBSTACLE_FOREROUND_COLOR);
				}
				else
				{
					if(coordWireID != Constants.FREE)
					{
						cell.setText("<html>#" + coordWireID + "<br>" + Constants.PATH_FACE + "</html>");
					}
					else
					{
						cell.setText(coordWireID.toString());
					}
					cell.setBackground(Constants.CELL_COLOR);
					cell.setForeground(Color.BLACK);
				}

				cell.setHorizontalAlignment(JLabel.CENTER);
				grid.add(cell);
			}
		}

		grid.setPreferredSize(new Dimension(numCols * Constants.CELL_DIM, numRows * Constants.CELL_DIM));
		grid.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // top, left, bottom, right


//		controls.add(Box.createRigidArea(new Dimension(30, 0)));
		showPath = new JCheckBox("Show paths");
		showPath.setFont(charFont);
		showPath.setFocusPainted(false);
		showPath.addItemListener(e -> repaint());


		JCheckBox clickMe = new JCheckBox("Click Me!");
		clickMe.setFont(charFont);
		clickMe.setFocusPainted(false);

		clickMe.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					JOptionPane.showMessageDialog(null, Constants.THANKS_MESSAGE, "Thanks",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		controlsLeft.add(clickMe);
		controlsLeft.add(showPath);


		JPanel info = new JPanel(new GridLayout(3, 0));
		JLabel file = new JLabel();
		file.setFont(charFont);
		file.setText(this.filename);

		totalWireUsage = new JLabel();
		totalWireUsage.setFont(charFont);
		totalWireUsage.setText("Total wire usage:");

		isSolvable = new JLabel();
		isSolvable.setFont(charFont);
		isSolvable.setText("");

		info.add(file);
		info.add(isSolvable);
		info.add(totalWireUsage);


		controls.add(controlsLeft);
		controls.add(info);



		main.add(grid, BorderLayout.CENTER);
		main.add(controls, BorderLayout.SOUTH);

		setContentPane(main);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Paint the GUI.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if(showPath.isSelected())
		{
			showLayout();
		}
	}


	private void showLayout()
	{
		long sleepTime = 500;

		Map<Integer, Path> layout = PathFinder.connectAllWires(chip);

		playPathAnimation(layout, sleepTime);

	}


	private void playPathAnimation(Map<Integer, Path> layout, long sleepTime)
	{

		String solvable;

		if(layout.size() == chip.wires.size())
		{
			solvable = "solvable";
		}
		else
		{
			if(layout.size() == 0)
			{
				solvable = "unsolvable";
			}
			else
			{
				solvable = "partially solvable";
			}
		}

		totalWireUsage.setFont(new Font("Courier", Font.BOLD, 20));
		totalWireUsage.setText("Total wire usage: " + PathFinder.totalWireUsage(layout));

		isSolvable.setFont(new Font("Courier", Font.BOLD, 20));

		isSolvable.setText(solvable);

		int row = 0, col = 0;
		Random rd = new Random();
		int max = 255;
		int min = 100;
		for(Wire wire: chip.wires)
		{
			Color randColor = new Color(rd.nextInt(max - min + 1) + min, rd.nextInt(max - min + 1) + min, rd.nextInt(max - min + 1) + min);
			if(layout.containsKey(wire.wireId))
			{
				Iterator<Coord> pathIterator = layout.get(wire.wireId).iterator();

				while (pathIterator.hasNext())
				{
					Coord coord = pathIterator.next();
					row = coord.getY();
					col = coord.getX();
					JLabel cell = cells[row][col];
					System.out.println(coord.toString());
					if(showPath.isSelected())
					{
						cell.setText("<html>#" + wire.wireId + "<br>" + Constants.PATH_FACE + "</html>");
						cell.setBackground(randColor);
						cell.setForeground(Color.BLUE);
					}
				}
			}
			else
			{
				Coord start = wire.from;
				Coord end = wire.to;
				JLabel cellStart = cells[start.getY()][start.getX()];
				JLabel cellEnd = cells[end.getY()][end.getX()];

				if(showPath.isSelected())
				{
					cellStart.setText("<html>#" + wire.wireId + "<br>" + Constants.NO_PATH_FACE + "</html>");
					cellStart.setBackground(Color.RED);
					cellStart.setForeground(Color.BLACK);

					cellEnd.setText("<html>#" + wire.wireId + "<br>" + Constants.NO_PATH_FACE + "</html>");
					cellEnd.setBackground(Color.RED);
					cellEnd.setForeground(Color.BLACK);
				}
			}
		}
	}
}
