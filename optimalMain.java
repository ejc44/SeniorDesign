import java.io.*;
import java.lang.Math;
import java.util.*;

public class optimalMain {
	public static void main(String [] args) throws IOException {
		int numVars = Integer.parseInt(args[0]);
		String truthTableFilename = args[1];

		String[] lines = readUserTableFile(numVars, truthTableFilename);
		int[] table = getTableFromFile(numVars,lines);
		int index = calcIndex(table);

		String indexFilename = "/"+numVars+"var/"+index+".txt";

		int[][] sumOfProducts;
		int sopTerms;
		BooleanTree network;

		try {
			BufferedReader buff = new BufferedReader(new FileReader(indexFilename));
			network = null;
		} catch (IOException e) {
			System.out.println("File does not exist. Creating SOP.\n");

			sumOfProducts = getSumOfProducts(numVars,table);
			sopTerms = getSOPTerms(table);
	
			network = new BooleanTree(numVars, table, sumOfProducts, sopTerms);
		}

		String s = network.printNetwork();
		System.out.println(s);
		int cost = network.getCost();
		System.out.println(cost);

		System.out.println();
		s = network.tree_string();
		System.out.println(s);
		
		/*network.mutate();
		System.out.println("Test");
		s = network.printNetwork();
		System.out.println(s);*/
		
		/*
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFilename));

		oos.writeObject(network);
		oos.flush();
		oos.close();
		*/

		// for(int i=0;i<5;i++) {
		// 	network.mutate();

		// 	cost = network.getCost();

		// 	s = network.printNetwork();
		// 	System.out.println(s);
		// 	System.out.println(cost);

		// }
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

	public static int calcIndex(int[] table) {
		int index = 0;
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