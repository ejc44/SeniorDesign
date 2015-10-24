import java.io.*;
import java.lang.Math;
import java.util.*;

public class optimalMain {
	public static void main(String [] args) throws IOException {
		int numVars = Integer.parseInt(args[0]);
		String truthTableFilename = args[1];

		String[] lines = readUserTableFile(numVars, truthTableFilename);
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

			network = new BooleanTree(numVars, table, linesFromFile);
		} catch (IOException e) {
			System.out.println("File does not exist. Creating SOP.\n");

			sumOfProducts = getSumOfProducts(numVars,table);
			sopTerms = getSOPTerms(table);
	
			network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);
		}


		String s = network.printNetwork();
		System.out.println(s);
		cost = network.getCost();
		System.out.println(cost);

		System.out.println();
		s = network.tree_string();
		System.out.println(s);

		s = network.fileOutput();
		System.out.println(s);

		for(int i=0;i<5;i++) {
			network.mutate();
			System.out.println(network.fileOutput());
			table = network.getTruthTable();

			index = calcIndex(table);
			indexFilename = basePath+"\\"+numVars+"var\\"+index+".txt";

			try {
				BufferedReader buff2 = new BufferedReader(new FileReader(indexFilename));

				ArrayList<String> linesFromFile = readDatabaseFile(buff2);
				buff2.close();

				cost = Integer.parseInt(linesFromFile.get(0));

				if(cost > network.getCost()) {
					FileWriter writer = new FileWriter(new File(indexFilename));
					writer.write(Integer.toString(network.getCost()) + "\n");
					writer.write(network.fileOutput());
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				FileWriter writer = new FileWriter(new File(indexFilename));
				writer.write(Integer.toString(network.getCost()) + "\n");
				writer.write(network.fileOutput());
				writer.flush();
				writer.close();
			}
		}
	}









	public static String[] readUserTableFile(int numVars, String filename) {
		String[] lines = new String[(int) (Math.pow(2,numVars))];
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filename));
			String line;
			int i=0;

			while((line=buff.readLine()) != null) {
				lines[i] = line;
				i++;
			}
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}

		return lines;
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

	public static int chooseTable(int numVars) {
		Random randGen = new Random(System.currentTimeMillis());
		int index = randGen.nextInt((int) (Math.pow(2,(Math.pow(2,numVars)))));

		return index;
	}
}