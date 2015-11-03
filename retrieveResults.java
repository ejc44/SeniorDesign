import java.io.*;
import java.lang.Math;
import java.util.*;
import java.text.*;

public class retrieveResults {
	public static void main(String [] args) throws IOException {
		long startTime = System.currentTimeMillis();

		double totalCosts = 0;
		double totalSOPCosts = 0;
		int numberNotFound = 0;
		String basePath = new File("").getAbsolutePath();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Calendar c = Calendar.getInstance();

		// 3 variables
		for(long i=0;i<(long) Math.pow(2,Math.pow(2,3));i++) {
			int sopCost = calcSOPCost(3,i);

			String indexFilename = basePath+"\\3var\\"+i+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost == -1) {
				numberNotFound++;
			} else {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
			}
		}
		double dbCostsAvg = totalCosts / (Math.pow(2,Math.pow(2,3))-numberNotFound);
		double sopCostsAvg = totalSOPCosts / (Math.pow(2,Math.pow(2,3))-numberNotFound);

		FileWriter f = new FileWriter(basePath+"\\3var\\testResults.csv",true);

		f.append(df.format(c.getTime())+",");
		f.append(dbCostsAvg+",");
		f.append(sopCostsAvg+",");
		f.append(numberNotFound+"\n");
		f.flush();
		f.close();

		totalCosts=0;
		totalSOPCosts=0;
		numberNotFound=0;


		// 4 variables
		for(long i=0;i<(long) Math.pow(2,Math.pow(2,4));i++) {
			int sopCost = calcSOPCost(4,i);

			String indexFilename = basePath+"\\4var\\"+i+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost == -1) {
				numberNotFound++;
			} else {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
			}
		}
		dbCostsAvg = totalCosts / (Math.pow(2,Math.pow(2,4))-numberNotFound);
		sopCostsAvg = totalSOPCosts / (Math.pow(2,Math.pow(2,4))-numberNotFound);

		f = new FileWriter(basePath+"\\4var\\testResults.csv",true);

		f.append(df.format(c.getTime())+",");
		f.append(dbCostsAvg+",");
		f.append(sopCostsAvg+",");
		f.append(numberNotFound+"\n");
		f.flush();
		f.close();

		totalCosts=0;
		totalSOPCosts=0;
		numberNotFound=0;

		// 5 variables
		for(long i=0;i<(long) Math.pow(2,Math.pow(2,5));i++) {
			int sopCost = calcSOPCost(5,i);

			String indexFilename = basePath+"\\5var\\"+i+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost == -1) {
				numberNotFound++;
			} else {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
			}
		}
		dbCostsAvg = totalCosts / (Math.pow(2,Math.pow(2,5))-numberNotFound);
		sopCostsAvg = totalSOPCosts / (Math.pow(2,Math.pow(2,5))-numberNotFound);

		f = new FileWriter(basePath+"\\5var\\testResults.csv",true);

		f.append(df.format(c.getTime())+",");
		f.append(dbCostsAvg+",");
		f.append(sopCostsAvg+",");
		f.append(numberNotFound+"\n");
		f.flush();
		f.close();

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime);
	}

	/********************
	Calculate the cost of a SOP representation of a truth table

	int numVars: number of variables corresponding to the truth table
	int[] table: integer array representing a truth table

	Return: integer value representing the cost of the sum of products 
	********************/
	public static int calcSOPCost(int numVars, long index) {
		int cost = 0;
		int andGates = 0;

		String s = Long.toBinaryString(index);

		for(int i=0;i<s.length();i++) {
			if(Character.getNumericValue(s.charAt(i))==1) {
				cost += numVars + 1;
				andGates++;
			}
		}

		cost += andGates + 1;

		return cost;
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
}