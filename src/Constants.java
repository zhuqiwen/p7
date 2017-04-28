import java.awt.*;

public interface Constants {
	public static final String TITLE = "Project 7: Routing Wires on a Chip";
	public static final String INPUTS_FOLDER = "inputs";
	public static final String EXTENSION = ".in";
	public static final String OBSTACLE_FACE = "<html>(+_+)<br>STOP!!</html>";
	public static final String PATH_FACE = "(^‿^)";
	public static final String NO_PATH_FACE = "(╥﹏╥)";
	public static final String THANKS_MESSAGE = "Thank you for the great help of this spring semester!\n" +
		"Thanks to Suzanne: \n" +
		"\tYour design of the lectures and assignments forces me to think a lot harder than I expected. \n\tI did learn a lot more than I expected.\n\n" +
		"Thanks to Deyaa:\n \tSorry for inconvenience caused by my incorrect repo submission.\n\n" +
		"Thanks to Ambrose: \n\tThanks for the excellent mid-term review.\n\n" +
		"Thanks to Sarah: \n\tYou are so patient and considerate. \n\n" +
		"Thanks to Ben, Jack, Qingyue, and Jena: \n\tThanks for your careful gradings.\n\n" +
		"And special thanks to my wifi for the suggestions of using ASCII emoticons";


	public static final int MAX_BOARD_SIZE = 250;
	public static final int FREE = 0;
	public static final int OBSTACLE = -1;
	public static final int CELL_DIM = 80;
	public static final Color OBSTACLE_BACKGROUND_COLOR = Color.BLACK;
	public static final Color OBSTACLE_FOREROUND_COLOR = Color.WHITE;
	public static final Color PATH_COLOR = Color.YELLOW;
	public static final Color CELL_COLOR = Color.WHITE;



}
