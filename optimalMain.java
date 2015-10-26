import java.io.*;
import java.lang.Math;
import java.util.*;

public class optimalMain {
	public static void main(String [] args) throws IOException {
		int numVars = Integer.parseInt(args[0]);
		String truthTableFilename = args[1];

		String[] lines = readUserTableFile(numVars, truthTableFilename);

		if(lines.length != ((int) (Math.pow(2,numVars))) ) {
			System.out.println("Error with user input:\nNumber of variables does not match up with the number of truth table values");
		} else {
			int[] table = getTableFromFile(numVars,lines);
			long index = calcIndex(table);

			String basePath = new File("").getAbsolutePath();
			String indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

			int[][] sumOfProducts;
			int sopTerms;
			BooleanTree network;

			int cost;

			try {
				BufferedReader buff = new BufferedReader(new FileReader(indexFilename));
				
				ArrayList<String> linesFromFile = readDatabaseFile(buff);
				buff.close();

				cost = Integer.parseInt(linesFromFile.get(0));
				linesFromFile.remove(0);

				int sopCost = calcSOPCost(numVars, table);

				// Check if the network in the file has a cost less than or equal to the SOP cost
				// If so, use that network
				// If not, use SOP network
				if(cost<=sopCost) {
					network = new BooleanTree(numVars, table, linesFromFile);
				} else {
					sumOfProducts = getSumOfProducts(numVars,table);
					sopTerms = getSOPTerms(table);
		
					network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);
				}
			} catch (IOException e) {
				System.out.println("File does not exist. Creating SOP.\n");

				sumOfProducts = getSumOfProducts(numVars,table);
				sopTerms = getSOPTerms(table);
		
				network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);
			}

			// Output Network
			String s = network.printNetwork();
			System.out.println(s);
			cost = network.getCost();
			System.out.println(cost);


			// Mutate the network		
			network.mutate();

			System.out.println(network.printNetwork());

			// Get new truth table, calculate new index, create new filename
			table = network.getTruthTable();
			index = calcIndex(table);
			indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

			// Check if file already exists
			try {
				BufferedReader buff2 = new BufferedReader(new FileReader(indexFilename));

				ArrayList<String> linesFromFile = readDatabaseFile(buff2);
				buff2.close();

				cost = Integer.parseInt(linesFromFile.get(0));

				// If the cost of the new network is less than the database network write the new network to the file
				if(cost > network.getCost()) {
					FileWriter writer = new FileWriter(new File(indexFilename));
					writer.write(Integer.toString(network.getCost()) + "\n");
					writer.write(network.fileOutput());
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				// Write the network to the file
				FileWriter writer = new FileWriter(new File(indexFilename));
				writer.write(Integer.toString(network.getCost()) + "\n");
				writer.write(network.fileOutput());
				writer.flush();
				writer.close();
			}
		}
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

	public static ArrayList<String> readDatabaseFile(BufferedReader buff) {
		ArrayList<String> lines = new ArrayList<String>();
		String line;

		try {
			// Read in the files into lines arraylist
			while((line=buff.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}
		return lines;
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

	public static long calcIndex(int[] table) {
		long index = 0;
		for(int i=0;i<table.length;i++) {
			index += table[i] * (int) (Math.pow(2,i));
		}

		return index;
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

	public static int chooseTable(int numVars) {
		Random randGen = new Random(System.currentTimeMillis());
		int index = randGen.nextInt((int) (Math.pow(2,(Math.pow(2,numVars)))));

		return index;
	}
}