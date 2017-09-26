package cellularAutomaton;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class cellularAutomaton {
	/* CLASS INITIALIZATION */
	static class cellData
	{
		int cellSize = 0;
		int nutrientSize = 0;
		int cancerCellSize = 0;
		int damagedCellSize = 0;
		int regularCellSize = 0;
	}
	static class coordinate
	{
		int x;
		int y;
		coordinate(int x, int y)
		{
			this.x = x;
			this.y = y;
		}		
		public boolean equals(coordinate coordinate) 
		{ 
			if((coordinate.x == this.x) && (coordinate.y == this.y)) 
				return true; 
			else return false; 
		}
	}
	static class nutrient
	{
		int x;
		int y;
		nutrient(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
	public enum cellState 
	{
		NORMAL, DAMAGED, CANCER, NONEXISTENT
	}
	static class cell implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2996868987930228332L;
		int x;
		int y;
		int numberTurnsSinceLastEating = 1;
		int nutrientConcentration = initialNutrientConcentration;
		cellState cellState = cellularAutomaton.cellState.NORMAL; 
		cell(int x, int y)
		{
			this.x = x;
			this.y = y;
			
		}
		public static Object copy(Object orig) {
	        Object obj = null;
	        try {
	            // Write the object out to a byte array
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            ObjectOutputStream out = new ObjectOutputStream(bos);
	            out.writeObject(orig);
	            out.flush();
	            out.close();

	            // Make an input stream from the byte array and read
	            // a copy of the object back in.
	            ObjectInputStream in = new ObjectInputStream(
	                new ByteArrayInputStream(bos.toByteArray()));
	            obj = in.readObject();
	        }
	        catch(IOException e) {
	            e.printStackTrace();
	        }
	        catch(ClassNotFoundException cnfe) {
	            cnfe.printStackTrace();
	        }
	        return obj;
	    }
	}
	
	/* CELL DATA INITIALIZATION */
	final static double nutrientKProportionOfGrid = 1;
	final static double nutrientStartProportionOfGrid = 0.5;
	final static double cellMigrationRate = 0.8;
	final static double nutrientMigrationRate = 0.8; 
	final static double nutrientUptakeRate = 0.8;
	final static double damageRate = 0.1;
	final static double damagedToNormalProbability = 0.5;
	final static double damagedToDeathProbability = 0.499;
	final static double effectOfNeighboringCellsOnCellDivision = 1.25;
	final static int numNutrientsNeededForDivision = 2;
	final static int numTimeStepsBeforeEating = 100;
	final static int initialNutrientConcentration = 1;
	final static int cancerCellStealingRadius = 1;
	
	/* PROGRAM PARAMETERS INITIALIZATION */
	final static int NUM_TRIALS = 2000;
	final static int BORDER_SIZE = 256;
	final static boolean takeAllNutrients = true;
	final static boolean nutrientMapOutput = true;
	final static boolean birthDeathMap = false;
	final static boolean createMovie = true;
	final static int movieImageFrequency = 1;
	final static int nutrientReplenishRate = 1;
	final static int nutrientAddLimitPerTurn = 200;
	final static int numTurnsBeforeCancerGrowth = 500;
	final static int nutrientMigrationConstant = 50; //controls how often nutrients migrate; higher is more migration.
	
	/* PROGRAM DATA INITIALIZATION */
	final static String ID = String.valueOf(System.nanoTime());
	static Random randomGen = new Random(System.nanoTime());
	static cellData[] cD = new cellData[NUM_TRIALS];
	static Vector<cell> cells = new Vector<cell>();
	static Vector<nutrient> nutrients = new Vector<nutrient>(); 
	final static int[][] nutrientMap = new int[BORDER_SIZE][BORDER_SIZE];
	final static boolean[][] cellMap = new boolean[BORDER_SIZE][BORDER_SIZE]; 
	final static int[][] cellNutrientMap = new int[BORDER_SIZE][BORDER_SIZE];
	final static cellState[][] cellStateMap = new cellState[BORDER_SIZE][BORDER_SIZE];
	final static int[][] birthMap = new int[BORDER_SIZE][BORDER_SIZE];
	final static int[][] deathMap = new int[BORDER_SIZE][BORDER_SIZE];
	
	/* INITIALIZATION FUNCTIONS */
	@SuppressWarnings("unused")
	private void checkForBadParameters()
	{
		try
		{
			if(nutrientKProportionOfGrid > 1)
				throw new Exception();
			if(nutrientStartProportionOfGrid > 1)
				throw new Exception();
			if(cellMigrationRate > 1)
				throw new Exception();
			if(nutrientUptakeRate > 1)
				throw new Exception();
			if(damageRate > 1)
				throw new Exception();
			if(damagedToNormalProbability + damagedToDeathProbability > 1)
				throw new Exception();
			if(numNutrientsNeededForDivision < 0)
				throw new Exception();
			if(numTimeStepsBeforeEating < 0)
				throw new Exception();
			if(nutrientMigrationConstant < 0)
				throw new Exception();
			if(numTurnsBeforeCancerGrowth < 0)
				throw new Exception();
			if(nutrientReplenishRate <= 0)
				throw new Exception();
			if(nutrientAddLimitPerTurn < 0)
				throw new Exception();
			if(BORDER_SIZE < 0)
				throw new Exception();
			if(NUM_TRIALS < 0)
				throw new Exception();
			if(movieImageFrequency <= 0 && createMovie)
				throw new Exception();
			if(nutrientMigrationConstant <= 0)
				throw new Exception();
			
		}
		catch(Exception e)
		{
	      		JOptionPane.showMessageDialog(null, "Check program parameters, refer to operation manual for details. Program Teriminating...", "Error", JOptionPane.ERROR_MESSAGE);
	        	System.exit(-1);
		}
	}
	private void initializeFirstCell()
	{
		cellMap[BORDER_SIZE/2][BORDER_SIZE/2] = true;	
		cell initialCell = new cell(BORDER_SIZE/2, BORDER_SIZE/2);
		cellStateMap[BORDER_SIZE/2][BORDER_SIZE/2] = cellState.NORMAL;
		cellNutrientMap[BORDER_SIZE/2][BORDER_SIZE/2] = initialNutrientConcentration;
		cells.add(initialCell);
	}
	private void fillNutrientAndCellDataArray()
	{
		for(int i = 0; i < BORDER_SIZE; i++)
			for(int j = 0; j < BORDER_SIZE; j++)
			{
				cellMap[i][j] = false;
				nutrientMap[i][j] = 0;
				cellNutrientMap[i][j] = 0;
				cellStateMap[i][j] = cellState.NONEXISTENT;
				if(birthDeathMap)
					birthMap[i][j] = deathMap[i][j] = 0;
			}
	}
	private static void resupplyNutrients(boolean start)
	{
		int nutrientLimit = (int) (BORDER_SIZE * BORDER_SIZE * nutrientKProportionOfGrid);
		int currentNutrientSize = nutrients.size();
		if(start)
		{
			nutrientLimit = (int) (BORDER_SIZE * BORDER_SIZE * nutrientStartProportionOfGrid);
			currentNutrientSize = 666_666_665;
		}		
		while(nutrients.size() < nutrientLimit && nutrients.size() < currentNutrientSize + nutrientAddLimitPerTurn)
		{
			int x = randomGen.nextInt(BORDER_SIZE);
			int y = randomGen.nextInt(BORDER_SIZE);
			nutrient nutrient = new nutrient(x, y);
			nutrients.add(nutrient);
			nutrientMap[x][y]++;
		}
	}
	private void fillCellDataArray() {
		for(int a = 0; a < NUM_TRIALS; a++)	
			cD[a] = new cellData();
	}	 
	
	/* CELL FUNCTION PROCESSING */
	private static void processCellDataCollection(int a)
	{
		cD[a].cellSize = cells.size();
		cD[a].nutrientSize = nutrients.size();
		int normalCellCount = 0;
		int damagedCellCount = 0;
		int cancerCellCount = 0;
		for(cell cell: cells)
		{
			if(cell.cellState == cellState.CANCER)
				cancerCellCount++;
			else if(cell.cellState == cellState.DAMAGED)
				damagedCellCount++;
			else
				normalCellCount++;
		}
		cD[a].cancerCellSize = cancerCellCount;
		cD[a].regularCellSize = normalCellCount;
		cD[a].damagedCellSize = damagedCellCount;
	}
	private static void processCellDeath()
	{
		Vector<cell> cellsToRemove = new Vector<cell>();
		for(cell cell : cells)
		{
			if(cell.nutrientConcentration == 0)
			{
				cellMap[cell.x][cell.y] = false;
				cellsToRemove.add(cell);
				if(birthDeathMap)
					deathMap[cell.x][cell.y]++;
				cellNutrientMap[cell.x][cell.y] = 0;
				cellStateMap[cell.x][cell.y] = cellState.NONEXISTENT;
			}
		}
		for(int i = 0; i < cellsToRemove.size(); i++)
		{
			cells.remove(cellsToRemove.elementAt(i));
		}
	}
	private static void processCellMigrationAndProliferation(int a)
	{
		int K = cells.size();
		for(int i = 0; i < K; i++)
		{
			double randomMigration = randomGen.nextDouble();
			double randomProliferation = randomGen.nextDouble();
			double randomProliferation2 = randomGen.nextDouble();
			if(cells.elementAt(i).nutrientConcentration >= numNutrientsNeededForDivision) 
				if(((Math.pow(effectOfNeighboringCellsOnCellDivision,(4 - numberEmptyCellsAround(cells.elementAt(i).x, cells.elementAt(i).y))) - 1 < randomProliferation) 
				&& randomProliferation2 < (1 - cells.size()/(BORDER_SIZE * BORDER_SIZE))) 
				|| (cells.elementAt(i).cellState == cellState.CANCER))  
				{
					proliferateCell(cells.elementAt(i), a);
				}
				else if(randomMigration < cellMigrationRate)
				{	
					migrateCell(cells.elementAt(i));
				}
			else if(randomMigration < cellMigrationRate)
			{	
				migrateCell(cells.elementAt(i));
			}
		}
	}
	private static void proliferateCell(cell cell, int a) 
	{	
		int numberEmptyCellsAround = numberEmptyCellsAround(cell.x,cell.y);
		if(numberEmptyCellsAround != 0)
		{
			double probUp;
			double probLeft;
			double probDown;
			cell.nutrientConcentration = initialNutrientConcentration;
			cellNutrientMap[cell.x][cell.y] = initialNutrientConcentration;
			cell.numberTurnsSinceLastEating = 1;
			if(cell.y != BORDER_SIZE - 1)
				probUp = (1 - (cellMap[cell.x][cell.y + 1]?1:0.0))/numberEmptyCellsAround;
			else probUp = 0;
			if(cell.y != 0)
				probDown = (1 - (cellMap[cell.x][cell.y - 1]?1:0.0))/numberEmptyCellsAround;
			else probDown = 0;
			if(cell.x != 0)
				probLeft = (1 - (cellMap[cell.x - 1][cell.y]?1:0.0))/numberEmptyCellsAround;
			else probLeft = 0;
			double proliferationRandom = randomGen.nextDouble();
			if(proliferationRandom <= probUp)
				duplicateCell(cell, cell.x, cell.y + 1, a); //up
			else if(proliferationRandom <= probUp + probLeft)
				duplicateCell(cell, cell.x - 1, cell.y, a); //left
			else if(proliferationRandom <= probUp + probLeft + probDown)
				duplicateCell(cell, cell.x, cell.y - 1, a); //down
			else 
				duplicateCell(cell, cell.x + 1, cell.y, a); //right
		}
		else 
		{
			double randomMigration = randomGen.nextDouble();
			if(randomMigration < cellMigrationRate)
				migrateCell(cell);
		}
	}
	private static void migrateCell(cell cell) 
	{
		int numberEmptyCellsAround = numberEmptyCellsAround(cell.x,cell.y);
		if(numberEmptyCellsAround != 0)
		{
			double probUp;
			double probLeft;
			double probDown;
			if(cell.y != BORDER_SIZE - 1)
				probUp = (1 - (cellMap[cell.x][cell.y + 1]?1:0.0))/numberEmptyCellsAround;
			else probUp = 0;
			if(cell.y != 0)
				probDown = (1 - (cellMap[cell.x][cell.y - 1]?1:0.0))/numberEmptyCellsAround;
			else probDown = 0;
			if(cell.x != 0)
				probLeft = (1 - (cellMap[cell.x - 1][cell.y]?1:0.0))/numberEmptyCellsAround;
			else probLeft = 0;
			double migrationRandom = randomGen.nextDouble();
			if(migrationRandom <= probUp)
				moveCell(cell, cell.x, cell.y, cell.x, cell.y + 1); //up
			else if(migrationRandom <= probUp + probLeft)
				moveCell(cell, cell.x, cell.y, cell.x - 1, cell.y); //left
			else if(migrationRandom <= probUp + probLeft + probDown)
				moveCell(cell, cell.x, cell.y, cell.x, cell.y - 1); //down
			else 
				moveCell(cell, cell.x, cell.y, cell.x + 1, cell.y); //right
		}
	}
	private static void duplicateCell(cell cell, int newX, int newY, int a)
	{
		cell newCell = (cellularAutomaton.cell) cellularAutomaton.cell.copy(cell);
		newCell.x = newX;
		newCell.y = newY;
		cellMap[newX][newY] = true;
		cellNutrientMap[newX][newY] = initialNutrientConcentration;
		cellStateMap[newCell.x][newCell.y] = cell.cellState;
		if(cell.cellState == cellState.NORMAL)
		{
			double randomDamage = randomGen.nextDouble();
			if(randomDamage < damageRate && a > numTurnsBeforeCancerGrowth)
			{
				newCell.cellState = cellState.DAMAGED;
				cellStateMap[newCell.x][newCell.y] = cellState.DAMAGED;
			}
		}
		if(birthDeathMap)
			birthMap[newX][newY]++;
		cells.addElement(newCell);
	}
	private static void moveCell(cell cell, int oldX, int oldY, int newX, int newY) 
	{
		cellMap[oldX][oldY] = false;
		cellNutrientMap[oldX][oldY] = 0;
		cellStateMap[oldX][oldY] = cellState.NONEXISTENT;
		cell.x = newX;
		cell.y = newY;
		cellStateMap[newX][newY] = cell.cellState;
		cellNutrientMap[newX][newY] = cell.nutrientConcentration;
		cellMap[newX][newY] = true;
	}
	private static int numberEmptyCellsAround(int x, int y) 
	{
		int ctr = 4;
		if(x != 0)
			if(cellMap[x - 1][y])
				ctr--;
		if(x != BORDER_SIZE - 1)
			if(cellMap[x + 1][y])
				ctr--;
		if(y != 0)
			if(cellMap[x][y - 1])
				ctr--;
		if(y != BORDER_SIZE - 1)
			if(cellMap[x][y + 1])
				ctr--;
		if(x == 0 || x == BORDER_SIZE - 1)
			ctr--;
		if(y == 0 || y == BORDER_SIZE - 1)
			ctr--;
		return ctr;
	}
	private static void processCellEating()
	{
		for(cell cell : cells)
		{
			if(cell.numberTurnsSinceLastEating % numTimeStepsBeforeEating == 0)
			{	
				cell.nutrientConcentration--;
				cellNutrientMap[cell.x][cell.y]--;
			}
			cell.numberTurnsSinceLastEating++;
		}
	}
	private static void processNutrientStealing() {
		Vector<Integer> dx = new Vector<Integer>();
		Vector<Integer> dy = new Vector<Integer>();
		for(int i = -cancerCellStealingRadius; i <= cancerCellStealingRadius; i++)
		{
			dx.add(i);
			dy.add(i);
		}
		for(int i = 0; i < BORDER_SIZE; i++)
			for(int j = 0; j < BORDER_SIZE; j++)
				if(cellStateMap[i][j] == cellState.CANCER)
				{	
					for(int x : dx)
					{
						for(int y : dy)
						{
							if(i + x >= 0 && i + x < BORDER_SIZE)
								if(j + y >= 0 && j + y < BORDER_SIZE)
									if(cellStateMap[i + x][j + y] == cellState.NORMAL && cellNutrientMap[i + x][j + y] > 0)
									{
										cellNutrientMap[i + x][j + y]--;
										cellNutrientMap[i][j]++;
									}
						}
					}
				}
		for(cell cell: cells)
		{
			cell.nutrientConcentration = cellNutrientMap[cell.x][cell.y];
		}
	}
	private static void processDamagedCellResolution() 
	{
		Vector<cell> cellsToRemove = new Vector<cell>();
		for(cell cell : cells)
		{
			double randomDamageResolution = randomGen.nextDouble();
			if(cell.cellState == cellState.DAMAGED)
			{
				if(randomDamageResolution < damagedToNormalProbability)
				{
					cell.cellState = cellState.NORMAL;
					cellStateMap[cell.x][cell.y] = cellState.NORMAL;
				}
				else if(randomDamageResolution < damagedToNormalProbability + damagedToDeathProbability)
				{
					cellsToRemove.add(cell);
					cellMap[cell.x][cell.y] = false;
					if(birthDeathMap)
						deathMap[cell.x][cell.y]++;
				}
				else
				{
					cell.cellState = cellState.CANCER;
					cellStateMap[cell.x][cell.y] = cellState.CANCER;
				}
			}
		}
		for(int i = 0; i < cellsToRemove.size(); i++)
		{
			nutrientMap[cellsToRemove.elementAt(i).x][cellsToRemove.elementAt(i).y] += cellsToRemove.elementAt(i).nutrientConcentration;
			cellNutrientMap[cellsToRemove.elementAt(i).x][cellsToRemove.elementAt(i).y] = 0;
			cellStateMap[cellsToRemove.elementAt(i).x][cellsToRemove.elementAt(i).y] = cellState.NONEXISTENT;
			cells.remove(cellsToRemove.elementAt(i));
			
		}
	}
	
	/* NUTRIENT-CELL INTERACTIONS PROCESSING */
	private static void processCellNutrientInteractions()
	{
		if(takeAllNutrients)
		{
			ArrayList<coordinate> interactionCoordinate = new ArrayList<coordinate>();
			for(int i = 0; i < BORDER_SIZE; i++)
				for(int j = 0; j < BORDER_SIZE; j++)
					if(cellMap[i][j] && nutrientMap[i][j] > 0)
					{
						double randomNutrientPickup = randomGen.nextDouble();
						if(randomNutrientPickup < nutrientUptakeRate)
						{
							coordinate coordinate = new coordinate(i, j);
							interactionCoordinate.add(coordinate);
						}
					}
			Vector<nutrient> nutrientsToRemove = new Vector<nutrient>();
			
			for(coordinate coordinate : interactionCoordinate)
			{
				for(cell cell : cells)
					if(coordinate.x == cell.x && coordinate.y == cell.y)
					{			
						cellNutrientMap[coordinate.x][coordinate.y]++;
						cell.nutrientConcentration++;
					}
			}
			Set<coordinate> set = new HashSet<coordinate>();
			set.addAll(interactionCoordinate);
			interactionCoordinate.clear();
			interactionCoordinate.addAll(set);
			for(coordinate coordinate : interactionCoordinate)
			{
				for(nutrient nutrient: nutrients)
				{	
					if(nutrient.x == coordinate.x && nutrient.y == coordinate.y)
					{	
						nutrientsToRemove.add(nutrient);
						nutrientMap[nutrient.x][nutrient.y]--;
					}
				}
			}	
			for(int i = 0; i < nutrientsToRemove.size(); i++)
			{
				nutrients.remove(nutrientsToRemove.elementAt(i));
			}
		}
		else
		{
			Vector<coordinate> interactionCoordinate = new Vector<coordinate>();
			for(int i = 0; i < BORDER_SIZE; i++)
				for(int j = 0; j < BORDER_SIZE; j++)
					if(cellMap[i][j] && nutrientMap[i][j] > 0)
					{
						double randomNutrientPickup = randomGen.nextDouble();
						if(randomNutrientPickup < nutrientUptakeRate)
						{
							coordinate coordinate = new coordinate(i, j);
							nutrientMap[i][j]--;
							interactionCoordinate.add(coordinate);
							cellNutrientMap[i][j]++;
						}
					}
			Vector<nutrient> nutrientsToRemove = new Vector<nutrient>();
			Vector<coordinate> alreadyRemoved = new Vector<coordinate>();
			for(coordinate coordinate : interactionCoordinate)
			{
				for(cell cell : cells)
					if(coordinate.x == cell.x && coordinate.y == cell.y)
						cell.nutrientConcentration++;
				
				for(nutrient nutrient: nutrients)
				{	
					boolean test = true;
					for(coordinate removed : alreadyRemoved)
						if(removed.x == nutrient.x && removed.y == nutrient.y)
							test = false;
					if(test)
					{
						if(nutrient.x == coordinate.x && nutrient.y == coordinate.y)
						{	
							alreadyRemoved.add(coordinate);
							nutrientsToRemove.add(nutrient);
						}
					}
				}
				
			}
			for(int i = 0; i < nutrientsToRemove.size(); i++)
			{
				nutrients.remove(nutrientsToRemove.elementAt(i));
			}
		}
	}
	
	/* NUTRIENT FUNCTION PROCESSING */
	private static void processNutrientMigration()
	{
		for(nutrient nutrient : nutrients)
		{
			double randomNutrientMigration = randomGen.nextDouble();
			if(randomNutrientMigration < nutrientMigrationRate)
			{
				migrateNutrient(nutrient);
			}	
		}
	}
	private static void migrateNutrient(nutrient nutrient) 
	{
		double numberMigrationLocations = 4 - numberBlockedSpacesAround(nutrient.x, nutrient.y);
		double probUp;
		double probLeft;
		double probDown;
		if(nutrient.y != BORDER_SIZE - 1)
			probUp = 1/numberMigrationLocations;
		else probUp = 0;
		if(nutrient.y != 0)
			probDown = 1/numberMigrationLocations;
		else probDown = 0;
		if(nutrient.x != 0)
			probLeft = 1/numberMigrationLocations;
		else probLeft = 0;
		double migrationRandom = randomGen.nextDouble();
		if(migrationRandom <= probUp)
			moveNutrient(nutrient, nutrient.x, nutrient.y, nutrient.x, nutrient.y + 1); //up
		else if(migrationRandom <= probUp + probLeft)
			moveNutrient(nutrient, nutrient.x, nutrient.y, nutrient.x - 1, nutrient.y); //left
		else if(migrationRandom <= probUp + probLeft + probDown)
			moveNutrient(nutrient, nutrient.x, nutrient.y, nutrient.x, nutrient.y - 1); //down
		else 
			moveNutrient(nutrient, nutrient.x, nutrient.y, nutrient.x + 1, nutrient.y); //right
	
	}
	private static void moveNutrient(nutrient nutrient, int x, int y, int newX, int newY) {
		nutrientMap[x][y]--;
		nutrient.x = newX;
		nutrient.y = newY;
		nutrientMap[newX][newY]++;
	}
	private static int numberBlockedSpacesAround(int x, int y) 
	{
		int ctr = 0;
		if(x == 0 || x == BORDER_SIZE - 1)
			ctr++;
		if(y == 0 || y == BORDER_SIZE - 1)
			ctr++;
		return ctr;
	}
	private static void processNutrientDistribution(int a) {
		if(a % nutrientReplenishRate == 0)
		{
			if(a == 0)
				resupplyNutrients(true);
			else
				resupplyNutrients(false);
		}
	}

	/* DATA OUTPUT AND DATA PROCESSING */
	private static void outputText() 
	{
	    try
	    {
		File txtOutput = null;
		txtOutput = new File(ID + ".txt");
	        PrintWriter pr = new PrintWriter(txtOutput);
	        for(int a = 0; a < NUM_TRIALS; a++)
	        {
	        	pr.print(cD[a].cellSize + " ");
	        	pr.println(cD[a].nutrientSize);
	        	pr.println(cD[a].regularCellSize);
	        	pr.println(cD[a].cancerCellSize);
	        	pr.println();
	        }	
	      	if(birthDeathMap)
	      		for(int i = 0; i < BORDER_SIZE; i++)
	      		{
	      			for(int j = 0; j < BORDER_SIZE; j++)
	      			{
	      				pr.print(birthMap[i][j] + " ");
	      			}
	      			pr.println();
	      		}
	      	if(birthDeathMap)
		      	for(int i = 0; i < BORDER_SIZE; i++)
				{
		 			for(int j = 0; j < BORDER_SIZE; j++)
					{
		  				pr.print(deathMap[i][j] + " ");
					}
		 			pr.println();
				}
	    		pr.println();
	    		pr.println("nutrientStartProportionOfGrid:" + nutrientStartProportionOfGrid);
	    		pr.println("nutrientKProportionOfGrid:" + nutrientKProportionOfGrid);
	    		pr.println("cellMigrationRate:" + cellMigrationRate);
	    		pr.println("nutrientMigrationRate:" + nutrientMigrationRate);
	    		pr.println("nutrientUptakeRate:" + nutrientUptakeRate);
	    		pr.println("damageRate:" + damageRate);
	    		pr.println("damagedToNormalProbability:" + damagedToNormalProbability);
	    		pr.println("damagedToDeathProbability:" + damagedToDeathProbability);
	    		pr.println("effectOfNeighboringCellsOnCellDivision:" + effectOfNeighboringCellsOnCellDivision);
	    		pr.println("numNutrientsNeededForDivision:" + numNutrientsNeededForDivision);
	    		pr.println("numTimeStepsBeforeEating:" + numTimeStepsBeforeEating);
	    		pr.println("initialNutrientConcentration:" + initialNutrientConcentration);
	    		pr.println("takeAllNutrients:" + takeAllNutrients);
	    		pr.println("NUM_TRIALS:" + NUM_TRIALS);
	    		pr.println("BORDER_SIZE:" + BORDER_SIZE);
	    		pr.println("birthDeathOutput" + birthDeathMap);
	    		pr.println("nutrientMapOutput:" + nutrientMapOutput);
	    		pr.println("cancerCellStealingRadius:" + cancerCellStealingRadius);
	    		pr.println("createMovie:" + createMovie);
	    		pr.println("movieImageFrequency:" + movieImageFrequency);
	    		pr.println("nutrientReplenishRate:" + nutrientReplenishRate);
	    		pr.println("nutrientAddLimitPerTurn:" + nutrientAddLimitPerTurn);
	    		pr.println("numTurnsBeforeCancerGrowth:" + numTurnsBeforeCancerGrowth);
	    		pr.println("nutrientMigrationConstant:" + nutrientMigrationConstant);
	    	
	    		pr.close();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }		
	    System.out.println(cells.size());
	}
	private static void outputImage() 
	{
		BufferedImage img = new BufferedImage(BORDER_SIZE * 4 * (1 + (nutrientMapOutput?1:0)), BORDER_SIZE * 4, BufferedImage.TYPE_INT_ARGB);
        	img = makeImage(img);
       	 	File f = null;
        	try
        	{
        		String fileName = ID + ".png";
        		f = new File(fileName);
        		ImageIO.write(img, "png", f);
        	}
        	catch(IOException e)
        	{
        		System.out.println(e);
        	}		
	}
	private static BufferedImage makeImage(BufferedImage img) 
	{
        	for(cell cell : cells)
        	{
        		Color cellColor = null;
			if(cell.cellState == cellState.CANCER)
				cellColor = Color.RED;
			else if(cell.cellState == cellState.DAMAGED)
				cellColor = Color.BLUE;
			else if(cell.nutrientConcentration > 1)
				cellColor = Color.PINK;
			else 
				cellColor = Color.GREEN;
			for(int i = 0; i < 4; i++)
				for(int j = 0; j < 4; j++)
				{
					img.setRGB((cell.x * 4) + i, (cell.y * 4) + j, convertColorToPixel(cellColor));
				}
        	}
        	if(nutrientMapOutput)   	
        		for(nutrient nutrient: nutrients)
        		{
				Color nutrientColor = null;
				if(nutrientMap[nutrient.x][nutrient.y] == 0)
					nutrientColor = Color.BLACK;
				if(nutrientMap[nutrient.x][nutrient.y] == 1)
					nutrientColor = Color.YELLOW;
				if(nutrientMap[nutrient.x][nutrient.y] == 2)
					nutrientColor = Color.PINK;
				if(nutrientMap[nutrient.x][nutrient.y] == 3)
					nutrientColor = Color.GRAY;
				if(nutrientMap[nutrient.x][nutrient.y] == 4)
					nutrientColor = Color.CYAN;
				if(nutrientMap[nutrient.x][nutrient.y] >= 5)
					nutrientColor = Color.MAGENTA;
        			for(int i = 0; i < 4; i++)
       					for(int j = 0; j < 4; j++)
       					{
       						img.setRGB(BORDER_SIZE * 4 + (nutrient.x * 4) + i, (nutrient.y * 4) + j, convertColorToPixel(nutrientColor));
       					}        
		}
        
		return img;
	}
	private static int convertColorToPixel(Color color)
	{
		return (color.getAlpha()<<24) | (color.getRed()<<16) | (color.getGreen()<<8) | (color.getBlue());
	}
	private static void processImages(int a) 
	{
		if(a % movieImageFrequency == 0 && createMovie)
		{
			BufferedImage img = new BufferedImage(BORDER_SIZE * 4 * (1 + (nutrientMapOutput?1:0)), BORDER_SIZE * 4, BufferedImage.TYPE_INT_ARGB);
			img = makeImage(img);
			File f = null;
	 	        try
	 	        {
	        		String fileName = (a/movieImageFrequency) + ".png";
	 	       		f = new File(fileName);
	        		ImageIO.write(img, "png", f);
	       		}
	        	catch(IOException e)
	        	{
	        		System.out.println(e);
	        	}	
		}		
	}
	private static void processOutput()
	{
		if(cells.size() == 0)
			main(null);
		else
		{
			outputImage();
			outputText();
		}
	}

	/* MAIN FUNCTIONS */
	private static void processTrials() {
		for(int a = 0; a < NUM_TRIALS; a++)
        	{	
			processNutrientDistribution(a);
			processCellDataCollection(a);
    			processCellEating();
    			processCellDeath();
    			for(int i = 0; i < nutrientMigrationConstant; i++)
    				processNutrientMigration();
			processCellMigrationAndProliferation(a);
			processCellNutrientInteractions();
			processImages(a);
			processDamagedCellResolution();
			processNutrientStealing();
			if(cells.size() == 0)
			{	
				a = 0;
				break;
        		}
		}
		processOutput();
	}
	public static void main(String[] args)
	{
		cellularAutomaton c = new cellularAutomaton();
		c.checkForBadParameters();
		c.fillNutrientAndCellDataArray();
		c.fillCellDataArray();
		c.initializeFirstCell();
    		processTrials();
	}
}
