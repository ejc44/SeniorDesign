import java.io.*;
import java.lang.Math;
import java.util.*;

public class optimalMain {
	public static void main(String [] args) throws IOException {
		String choice;
		Scanner user_input = new Scanner(System.in);

		String basePath = new File("").getAbsolutePath();

		do {
			System.out.println("");
			System.out.println("2: Retrieve a network");
			System.out.println("1: Optimize database");
			System.out.println("0: Quit");
			choice = user_input.next();

			while(!choice.equals("0") && !choice.equals("1") && !choice.equals("2")) {
				System.out.println("Invalid choice. Please select either 0, 1, or 2.");
				choice = user_input.next();
			}

			if(choice.equals("2")) {
				String u;
				System.out.println("Number of variables (3-5):");
				u = user_input.next();

				while(!u.equals("3") && !u.equals("4") && !u.equals("5")) {
					System.out.println("Invalid choice. Please select either 3, 4, or 5.");
					u = user_input.next();
				}

				int numVars = Integer.parseInt(u);

				System.out.println("Truth table file:");
				String truthTableFilename = user_input.next();

				String[] lines = readUserTableFile(numVars,truthTableFilename);

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

					if(cost == -1) {
						System.out.println("File does not exist. Creating SOP.\n");

						sumOfProducts = getSumOfProducts(numVars,table);
						sopTerms = getSOPTerms(table);

						network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);

						sopCreated = true;
					} else if (cost>sopCost) {
						System.out.println("Database network cost greater than SOP cost. Creating SOP\n");

						sumOfProducts = getSumOfProducts(numVars,table);
						sopTerms = getSOPTerms(table);

						network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);

						sopCreated = true;
					} else {
						ArrayList<String> linesFromFile = readDatabaseFile(indexFilename);

						linesFromFile.remove(0);

						network = new BooleanTree(numVars, table, linesFromFile);
					}

					// Output Network
					String s = network.printNetwork();
					System.out.println(s);
					cost = network.getCost();
					System.out.println(cost);

					boolean success = writeDBFile(network,indexFilename);

					if(success) {
						System.out.println("Network written to database");
					}


					for(int i=0;i<50;i++) {
						network.mutate();

						// Output Network
						s = network.printNetwork();
						System.out.println(s);
						cost = network.getCost();
						System.out.println(cost);

						sopCost = calcSOPCost(numVars, network.getTruthTable());

						if(network.getCost() < sopCost) {
							index = calcIndex(network.getTruthTable());
							indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

							cost = getIndexCost(indexFilename);

							if(network.getCost() < cost || cost == -1) {
								success = writeDBFile(network, indexFilename);
								if(success) {
									System.out.println("Network written to database");
								}
							}
						}
					}
				}
			} else if(choice.equals("1")) {
				for(int i=0;i<5;i++) {
					String u;
					System.out.println("Number of variables (3-5):");
					u = user_input.next();

					while(!u.equals("3") && !u.equals("4") && !u.equals("5")) {
						System.out.println("Invalid choice. Please select either 3, 4, or 5.");
						u = user_input.next();
					}

					int numVars = Integer.parseInt(u);

					long index = chooseIndex(numVars);
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

					ArrayList<String> linesFromFile = readDatabaseFile(indexFilename);
					linesFromFile.remove(0);

					BooleanTree network = new BooleanTree(numVars,table,linesFromFile);

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
								}
							}
						}
					}
				}
			}

		} while(!choice.equals("0"));
	}


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

	public static int[] getTableFromFile(int numVars,String[] lines) {
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

	public static long calcIndex(int[] table) {
		long index = 0;
		for(int i=0;i<table.length;i++) {
			index += table[i] * (int) (Math.pow(2,i));
		}

		return index;
	}

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

	public static int getSOPTerms(int[] table) {
		int sopTerms =0;
		for(int i=0;i<table.length;i++) {
			if(table[i] == 1) {
				sopTerms++;
			}
		}
		return sopTerms;
	}

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

	public static int chooseNumVar() {
		Random randGen = new Random(System.currentTimeMillis());
		int numVar = randGen.nextInt(3) + 3;

		return numVar;
	}

	public static int chooseIndex(int numVars) {

		Random randGen = new Random(System.currentTimeMillis());
		int index = randGen.nextInt((int) (Math.pow(2,(Math.pow(2,numVars)))));

		return index;
	}

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


	public static int[] getTableFromIndex(int numVars, long index) {
		int[] table = new int[(int) (Math.pow(2,numVars))];
		String s = Long.toBinaryString(index);
		for(int i=0;i<s.length();i++) {
			table[i] = Character.getNumericValue(s.charAt(i));
		}

		return table;
	}

}