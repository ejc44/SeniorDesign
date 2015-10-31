import java.io.*;
import java.lang.Math;
import java.util.*;

public class optimalMain {
	public static void main(String [] args) throws IOException {
		String choice;	// Holds user selection
		Scanner user_input = new Scanner(System.in);

		String basePath = new File("").getAbsolutePath();

		// Create lists of the indexes we already have in the DB
		List<Long> threeVarIndexes = new ArrayList<>();
		threeVarIndexes = getDBIndexes(3);
		List<Long> fourVarIndexes = new ArrayList<>();
		fourVarIndexes = getDBIndexes(4);
		List<Long> fiveVarIndexes = new ArrayList<>();
		fiveVarIndexes = getDBIndexes(5);

		// List of indexes
		long[] threeVarCosts = new long[10];
		threeVarCosts = getDBCosts(3);
		long[] fourVarCosts = new long[10];
		fourVarCosts = getDBCosts(4);
		long[] fiveVarCosts = new long[10];
		fiveVarCosts = getDBCosts(5);

		do {
			// Ask user what they would like to do
			System.out.println("");
			System.out.println("2: Retrieve a network");
			System.out.println("1: Optimize database");
			System.out.println("0: Quit");
			choice = user_input.next();

			// Check if the user selected one of the possible options
			// Prompt them to choose again if not one of the options
			while(!choice.equals("0") && !choice.equals("1") && !choice.equals("2")) {
				System.out.println("Invalid choice. Please select either 0, 1, or 2.");
				choice = user_input.next();
			}

			// Retrieve network from Database, and mutate that network 50 times
			if(choice.equals("2")) {
				String u;
				// Ask user for number of variables
				System.out.println("Number of variables (3-5):");
				u = user_input.next();

				// Check if user selected a value between 3 and 5
				// Prompt user to choose again if invalid entry
				while(!u.equals("3") && !u.equals("4") && !u.equals("5")) {
					System.out.println("Invalid choice. Please select either 3, 4, or 5.");
					u = user_input.next();
				}

				int numVars = Integer.parseInt(u);

				// Ask user from truth table file
				System.out.println("Truth table file:");
				String truthTableFilename = user_input.next();

				// Read in truth table file
				String[] lines = readUserTableFile(numVars,truthTableFilename);

				// Check to see if numVars matches up with the number of number of rows in truth table
				if(lines.length != ((int) (Math.pow(2,numVars)))) {
					System.out.println("Error with user input:\nNumber of variables do not match with the number of truth table values");
				} else {
					boolean sopCreated = false;
					int[] table = getTableFromFile(numVars,lines);
					long index = calcIndex(table);

					String indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

					int[][] sumOfProducts;
					int sopTerms;
					BooleanTree network;

					int cost;

					cost = getIndexCost(indexFilename);

					int sopCost = calcSOPCost(numVars,table);

					// User truth table doesn't exist in database
					// Create SOP
					if(cost == -1) {
						System.out.println("File does not exist. Creating SOP.\n");

						sumOfProducts = getSumOfProducts(numVars,table);
						sopTerms = getSOPTerms(table);

						network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);

						sopCreated = true;
					} else if (cost>sopCost) {	// Cost in DB is greater than SOP, Create SOP
						System.out.println("Database network cost greater than SOP cost. Creating SOP\n");

						sumOfProducts = getSumOfProducts(numVars,table);
						sopTerms = getSOPTerms(table);

						network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);

						sopCreated = true;
					} else {	// Truth table exists in DB, read it in
						ArrayList<String> linesFromFile = readDatabaseFile(indexFilename);

						linesFromFile.remove(0);

						network = new BooleanTree(numVars, table, linesFromFile);
					}

					// Output Network to user
					String s = network.printNetwork();
					System.out.println(s);
					cost = network.getCost();
					System.out.println(cost);

					// Write file to db
					boolean success = writeDBFile(network,indexFilename);
					if(success) {
						System.out.println("Network written to database");

						// Add the truth table to list of things in db
						if(numVars==3) {
							if(!threeVarIndexes.contains(index)) {
								threeVarIndexes.add(index);
							}
						} else if(numVars == 4) {
							if(!fourVarIndexes.contains(index)) {
								fourVarIndexes.add(index);
							}
						} else {
							if(!fiveVarIndexes.contains(index)) {
								fiveVarIndexes.add(index);
							}
						}
					}

					// Mutate the user entered truth table network 50 times
					for(int i=0;i<50;i++) {
						network.mutate();

						// Output Network
						//s = network.printNetwork();
						//System.out.println(s);
						cost = network.getCost();
						//System.out.println(cost);

						sopCost = calcSOPCost(numVars, network.getTruthTable());

						// Check if the cost is less than SOP
						// Only save to DB if less than SOP
						if(network.getCost() < sopCost) {
							index = calcIndex(network.getTruthTable());
							indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

							cost = getIndexCost(indexFilename);

							// See if the cost is less than the cost currently in db
							if(network.getCost() < cost || cost == -1) {
								// Write to DB
								success = writeDBFile(network, indexFilename);
								if(success) {
									System.out.println("Network written to database");

									// Add the truth table to list of things in db
									if(numVars==3) {
										if(!threeVarIndexes.contains(index)) {
											threeVarIndexes.add(index);
										}
									} else if(numVars == 4) {
										if(!fourVarIndexes.contains(index)) {
											fourVarIndexes.add(index);
										}
									} else {
										if(!fiveVarIndexes.contains(index)) {
											fiveVarIndexes.add(index);
										}
									}
								}
							}
						}
					}
				}
			} else if(choice.equals("1")) {	// Optimize DB
				String u;
				// Prompt user to select DB folder to optimize
				System.out.println("Number of variables (3-5):");
				u = user_input.next();

				// Check if user selected a value between 3 and 5
				// Prompt user to choose again if invalid entry
				while(!u.equals("3") && !u.equals("4") && !u.equals("5")) {
					System.out.println("Invalid choice. Please select either 3, 4, or 5.");
					u = user_input.next();
				}
				int numVars = Integer.parseInt(u);

				// Choose 5 different networks to mutate 100 times each
				for(int i=0;i<5;i++) {
					// Select a network to mutate
					int indexListVal = chooseIndex(10);
					long index;
					if(numVars==3) {
						index = threeVarCosts[indexListVal];
					} else if(numVars==4) {
						index = fourVarCosts[indexListVal];
					} else {
						index = fiveVarCosts[indexListVal];
					}

					if(index!=0) {
						int[] table = getTableFromIndex(numVars,index);

						String indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

						int cost = getIndexCost(indexFilename);
						int loops = 0;
						while(cost == -1 && loops<=10) {
							index = chooseIndex(numVars);
							indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";
							cost = getIndexCost(indexFilename);
							loops++;
						}

						if(loops>=5) {
							break;
						}

						// Read in DB file
						// Remove cost from file inputs
						// Create network
						ArrayList<String> linesFromFile = readDatabaseFile(indexFilename);
						linesFromFile.remove(0);
						BooleanTree network = new BooleanTree(numVars,table,linesFromFile);

						// Mutate network 100 times
						for(int j=0;j<100;j++) {
							network.mutate();

							// Output Network
							//System.out.println(network.printNetwork());
							cost = network.getCost();
							//System.out.println(network.getCost());

							int sopCost = calcSOPCost(numVars, network.getTruthTable());

							if(network.getCost() < sopCost) {
								index = calcIndex(network.getTruthTable());
								indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

								cost = getIndexCost(indexFilename);

								if(network.getCost() < cost || cost == -1) {
									boolean success = writeDBFile(network, indexFilename);
									if(success) {
										System.out.println("Network written to database");

										if(numVars==3) {
											if(!threeVarIndexes.contains(index)) {
												threeVarIndexes.add(index);
											}
										} else if(numVars == 4) {
											if(!fourVarIndexes.contains(index)) {
												fourVarIndexes.add(index);
											}
										} else {
											if(!fiveVarIndexes.contains(index)) {
												fiveVarIndexes.add(index);
											}
										}
									}
								}
							}
						}
					} else {
						System.out.println("No cost file. Please run the program with a truth table.");
					}
				}
			}

		} while(!choice.equals("0"));

		// Save index lists
		saveDBIndexes(3, threeVarIndexes);
		saveDBIndexes(4, fourVarIndexes);
		saveDBIndexes(5, fiveVarIndexes);

		// Save highest and lowest indexes
		saveCostFile(3, threeVarIndexes);
		saveCostFile(4, fourVarIndexes);
		saveCostFile(5, fiveVarIndexes);
	}


	/********************
	Read in user's truth table file.

	int numVars: number of variables corresponding to the truth table
	String filename: filepath of the truth table file entered by user

	Return: a String array of all the lines from the file
	********************/
	public static String[] readUserTableFile(int numVars, String filename) {
		ArrayList<String> lines = new ArrayList<String>();
		int i=0;
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filename));
			String line;

			while((line=buff.readLine()) != null) {
				lines.add(line);
				i++;
			}
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}
		String[] linesArr = new String[i];

		return lines.toArray(linesArr);
	}

	/********************
	Using the string array retrieved from readUserTableFile(),
	convert to an integer array which represents the truth table

	int numVars: number of varaibles corresponding to the truth table
	String[] lines: string array returned from readUserTableFile()

	Return: an integer array which corresponds to the truth table
	********************/
	public static int[] getTableFromFile(int numVars, String[] lines) {
		int[] table = new int[(int) (Math.pow(2,numVars))];
		for(int i=0;i<lines.length;i++) {
			String line = lines[i];
			if(line.indexOf(",") > 0) {
				String [] hold = line.split(",");

				table[i] = Integer.parseInt(hold[numVars]);
			} else {
				table[i] = Integer.parseInt(line);
			}
		}
		
		return table;
	}

	/********************
	Calculate the DB index value of a truth table
	Basically a boolean to decimal conversion

	int[] table: integer array corresponding to a truth table

	Return: a long value which can be used to reference DB files
	********************/
	public static long calcIndex(int[] table) {
		long index = 0;
		for(int i=0;i<table.length;i++) {
			index += table[i] * (int) (Math.pow(2,i));
		}

		return index;
	}

	/********************
	Find the cost of a network from it's DB file

	String indexFilename: filepath of the db file to get the cost of

	Return: integer value of cost retrieved from DB file
		will return -1 if DB file does not exist, or errors in any other way
	********************/
	public static int getIndexCost(String indexFilename) {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(indexFilename));

			String s = buff.readLine();
			buff.close();

			int cost = Integer.parseInt(s);

			return cost;
		} catch(IOException e) {
			return -1;
		}
	} 

	/********************
	Create Sum-Of-Products array

	int numVars: number of varaibles corresponding to the truth table
	int[] table: integer array representing a truth table

	Return: multidimensional integer array which contains 0's and 1's representing each SOP term
	********************/
	public static int[][] getSumOfProducts(int numVars, int[] table) {
		int[][] sumOfProducts = new int[(int) (Math.pow(2,numVars))][numVars];
		int sopTerms=0;

		for(int i=0;i<table.length;i++) {
			if(table[i]==1) {
				String s = Integer.toBinaryString(i);

				if(s.length() ==numVars) {
					for(int j=0;j<numVars;j++) {
						sumOfProducts[sopTerms][j]=Character.getNumericValue(s.charAt(j));
					}
				} else {
					int zeros = numVars - s.length();

					for(int j=0;j<numVars;j++) {
						if(j<zeros) {
							sumOfProducts[sopTerms][j]=0;
						} else {
							sumOfProducts[sopTerms][j]=Character.getNumericValue(s.charAt(j-zeros));
						}
					}
				}
				sopTerms++;
			}
		}
		return sumOfProducts;
	}

	/********************
	Calculate the number of Sum-Of-Product terms

	int[] table: integer array representing a truth table

	Return: integer value which is the number of SOP terms
	********************/
	public static int getSOPTerms(int[] table) {
		int sopTerms =0;
		for(int i=0;i<table.length;i++) {
			if(table[i] == 1) {
				sopTerms++;
			}
		}
		return sopTerms;
	}

	/********************
	Read the lines of a DB file into an arraylist

	String indexFilename: filepath of a db file

	Return: arraylist containing all the lines of a DB file
	********************/
	public static ArrayList<String> readDatabaseFile(String indexFilename) {
		ArrayList<String> lines = new ArrayList<String>();
		String line;

		try {
			BufferedReader buff = new BufferedReader(new FileReader(indexFilename));
			// Read in the files into lines arraylist
			while((line=buff.readLine()) != null) {
				lines.add(line);
			}
			buff.close();
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}
		return lines;
	}

	/********************
	Calculate the cost of a SOP representation of a truth table

	int numVars: number of variables corresponding to the truth table
	int[] table: integer array representing a truth table

	Return: integer value representing the cost of the sum of products 
	********************/
	public static int calcSOPCost(int numVars, int[] table) {
		int cost = 0;
		int andGates = 0;
		for(int i=0;i<table.length;i++) {
			if(table[i]==1) {
				cost += numVars + 1;
				andGates++;
			}
		}

		cost += andGates + 1;

		return cost;
	}

	/********************
	Generate a random value between 0 and possible

	int possible: number of possible values to choose from

	Return: random integer between 0 and possible
	********************/
	public static int chooseIndex(int possible) {
		Random randGen = new Random(System.currentTimeMillis());
		int index = randGen.nextInt(possible);

		return index;
	}

	/********************
	Write file to DB

	BooleanTree network: booleantree representation of network to output
	String indexFilename: filepath of the location to output the network

	Return: boolean value of success
		true: network has been written to db
		false: error with writing network
	********************/
	public static boolean writeDBFile(BooleanTree network, String indexFilename) {
		try {
			FileWriter writer = new FileWriter(new File(indexFilename));

			writer.write(Integer.toString(network.getCost())+"\n");
			writer.write(network.fileOutput());
			writer.flush();
			writer.close();

			return true;
		} catch (IOException e) {
			return false;
		}

	}

	/********************
	Generate a truth table based on a db index value

	int numVars: number of input variables corresponding to the truth table
	long index: decimal value representing the truth table

	Return: integer array representing a truth table
	********************/
	public static int[] getTableFromIndex(int numVars, long index) {
		int[] table = new int[(int) (Math.pow(2,numVars))];
		String s = Long.toBinaryString(index);
		for(int i=0;i<s.length();i++) {
			table[i] = Character.getNumericValue(s.charAt(i));
		}

		return table;
	}

	/********************
	Create a list of all the index values in the DB

	int numVars: number of input variables corresponding to the DB

	Return: list of longs containing all index values in the DB
	********************/
	public static List<Long> getDBIndexes(int numVars) {
		List<Long> indexes = new ArrayList<>();

		String basepath = new File("").getAbsolutePath();
		String dbFilePath = basepath+"\\"+numVars+"var\\indexes.txt";

		File dbFile = new File(dbFilePath);

		if(dbFile.exists() == true) {
			try {
				BufferedReader buff = new BufferedReader(new FileReader(dbFilePath));
				String line;

				while((line=buff.readLine())!= null) {
					long hold = Long.valueOf(line).longValue();
					indexes.add(hold);
				}
			} catch (IOException e) {
				System.out.println("Error reading DB index file");
			}
		} else {
			for(long i=0;i<(long) Math.pow(2,Math.pow(2,numVars));i++) {
				String indexFilename = basepath+"\\"+numVars+"var\\"+i+".txt";
				int hold = getIndexCost(indexFilename);

				//System.out.println(i);

				if(hold > 0) {
					indexes.add(i);
				}
			}
		}

		return indexes;
	}

	/********************
	Save the list of indexes to a file in the db

	int numVars: number of input variables corresponding to the truth table
	List<Long> indexes: list of indexes in db
	********************/
	public static void saveDBIndexes(int numVars, List<Long> indexes) {
		String basepath = new File("").getAbsolutePath();
		String dbFilePath = basepath+"\\"+numVars+"var\\indexes.txt";

		try {
			FileWriter writer = new FileWriter(new File(dbFilePath));

			for(int i=0;i<indexes.size();i++) {
				writer.write(Long.toString(indexes.get(i))+"\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.out.println("Error saving DB index file");
		}
	}

	/********************
	Save the indexes which correspond to the 5 lowest costs and 5 highest costs
	to a file in the DB

	int numVars: number of input variables corresponding to the truth table
	List<Long> indexes: list of indexes in db
	********************/
	public static void saveCostFile(int numVars, List<Long> indexes) {
		String basepath = new File("").getAbsolutePath();
		String dbFilePath = basepath+"\\"+numVars+"var\\costs.txt";

		long[][] costs = new long[indexes.size()][2];
		int[] costArr = new int[indexes.size()];

		List<Long> organized = new ArrayList<>();

		for(int i=0;i<indexes.size();i++) {
			costs[i][0] = indexes.get(i);

			String indexFilename = basepath+"\\"+numVars+"var\\"+indexes.get(i)+".txt";
			costs[i][1] = getIndexCost(indexFilename);
			costArr[i] = getIndexCost(indexFilename);
		}

		Arrays.sort(costArr);

		for(int i=0;i<costArr.length;i++) {
			for(int j=0;j<costArr.length;j++) {
				if(costs[j][1] == costArr[i] && !organized.contains(costs[j][0])) {
					organized.add(costs[j][0]);
				}
			}
		}	

		try {
			FileWriter writer = new FileWriter(new File(dbFilePath));

			for(int i=0;i<5;i++) {
				writer.write(Long.toString(organized.get(i))+"\n");
			}
			for(int i=(organized.size()-1);i>(organized.size()-1-5);i--) {
				writer.write(Long.toString(organized.get(i))+"\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.out.println("Error saving cost file");
		}
	}

	/********************
	Creates an array containing the highest and lowest costs for the specified number of variables

	int numVars: number of input variables corresponding to the truth table

	return long[]: array of indexes
	********************/
	public static long[] getDBCosts(int numVars) {
		long[] indexes = new long[10];

		String basepath = new File("").getAbsolutePath();
		String dbFilePath = basepath+"\\"+numVars+"var\\costs.txt";

		File dbFile = new File(dbFilePath);

		int i=0;

		if(dbFile.exists() == true) {
			try {
				BufferedReader buff = new BufferedReader(new FileReader(dbFilePath));
				String line;

				while((line=buff.readLine())!= null) {
					long hold = Long.valueOf(line).longValue();
					indexes[i]=hold;

					i++;
				}
			} catch (IOException e) {
				System.out.println("Error reading DB index file");
			}
		} else {
			for(int j=0;j<10;j++) {
				indexes[j]=0;
			}
		}

		return indexes;
	}
}