import java.io.*;
import java.lang.Math;
import java.util.*;
import java.text.*;

public class retrieveResults {
	public static void main(String [] args) throws IOException {
		long startTime = System.currentTimeMillis();

		double totalCosts = 0;
		double totalSOPCosts = 0;
		int numberFound = 0;
		String basePath = new File("").getAbsolutePath();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Calendar c = Calendar.getInstance();

		List<Long> threeVarIndexes = new ArrayList<>();
		threeVarIndexes = getDBIndexes(3);
		List<Long> fourVarIndexes = new ArrayList<>();
		fourVarIndexes = getDBIndexes(4);
		List<Long> fiveVarIndexes = new ArrayList<>();
		fiveVarIndexes = getDBIndexes(5);

		// 3 variables
		for(long i=0;i<threeVarIndexes.size();i++) {
			long index = threeVarIndexes.get((int) i);
			int sopCost = calcSOPCost(3,index);

			String indexFilename = basePath+"\\3var\\"+index+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost > 0) {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
				numberFound++;
			}
		}

		double dbCostsAvg = totalCosts / numberFound;
		double sopCostsAvg = totalSOPCosts / numberFound;

		FileWriter f = new FileWriter(basePath+"\\3var\\testResults.csv",true);

		int numberNotFound = (int) Math.pow(2,Math.pow(2,3))-numberFound;

		f.append(df.format(c.getTime())+",");
		f.append(dbCostsAvg+",");
		f.append(sopCostsAvg+",");
		f.append(numberNotFound+"\n");
		f.flush();
		f.close();

		totalCosts=0;
		totalSOPCosts=0;
		numberFound=0;


		// 4 variables
		for(long i=0;i<fourVarIndexes.size();i++) {
			long index = fourVarIndexes.get((int) i);
			int sopCost = calcSOPCost(4,index);

			String indexFilename = basePath+"\\4var\\"+index+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost > 0) {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
				numberFound++;
			}
		}

		dbCostsAvg = totalCosts / numberFound;
		sopCostsAvg = totalSOPCosts / numberFound;

		f = new FileWriter(basePath+"\\4var\\testResults.csv",true);

		numberNotFound = (int) Math.pow(2,Math.pow(2,4))-numberFound;

		f.append(df.format(c.getTime())+",");
		f.append(dbCostsAvg+",");
		f.append(sopCostsAvg+",");
		f.append(numberNotFound+"\n");
		f.flush();
		f.close();

		totalCosts=0;
		totalSOPCosts=0;
		numberFound=0;

		// 5 variables
		for(long i=0;i<fiveVarIndexes.size();i++) {
			long index = fiveVarIndexes.get((int) i);
			int sopCost = calcSOPCost(5,index);

			String indexFilename = basePath+"\\5var\\"+index+".txt";
			int indexCost = getIndexCost(indexFilename);

			if(indexCost > 0) {
				totalCosts += indexCost;
				totalSOPCosts += sopCost;
				numberFound++;
			}
		}

		dbCostsAvg = totalCosts / numberFound;
		sopCostsAvg = totalSOPCosts / numberFound;

		f = new FileWriter(basePath+"\\5var\\testResults.csv",true);

		numberNotFound = (int) Math.pow(2,Math.pow(2,5))-numberFound;

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

}