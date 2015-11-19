import java.io.*;
import java.lang.Math;


public class readFileEqual {
	public static void main(String [] args) throws IOException {
		int numVars = Integer.parseInt(args[0]);
		String filename= args[1];
		
		BufferedReader buff=null;
		
		int[][] sumOfProducts = new int[(int) (Math.pow(2,numVars))][numVars];
		int[] table = new int[(int) (Math.pow(2,numVars))];

		int convert=0;

		int sopTerms=0;
		
		try {
			buff=new BufferedReader (new FileReader(filename));
			String line;
			int i=0;
			
			while((line=buff.readLine()) != null) {
				if(line.indexOf(",") > 0) {
					String [] hold = line.split(",");

					table[i]=Integer.parseInt(hold[numVars]);

					if(Integer.parseInt(hold[numVars]) == 1) {
						for(int j=0;j<numVars;j++) {
							sumOfProducts[sopTerms][j] = Integer.parseInt(hold[j]);
						}
						sopTerms++;
					}
					i++;
				} else {
					convert = 1;

					table[i]=Integer.parseInt(line);
					i++;
				}
			}
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}

		if(convert == 1) {
			int j=0;
		
			for(int i=0;i<table.length;i++) {
				if(table[i]==1) {
					String s = Integer.toBinaryString(i);
					
					if(s.length()==numVars) {
						for(int k=0;k<numVars;k++) {
							sumOfProducts[j][k]=Character.getNumericValue(s.charAt(k));
						}
					} else {
						int zeros = numVars-s.length();

						for(int k=0;k<numVars;k++) {
							if(k<zeros) {
								sumOfProducts[j][k]=0;
							} else {
								sumOfProducts[j][k]=Character.getNumericValue(s.charAt(k-zeros));
							}
						}
					}
					j++;
					sopTerms++;
				}
			}
		}
		
		BooleanTreeEqual test = new BooleanTreeEqual(numVars, table, sumOfProducts, sopTerms); // Generate the BooleanTreeEqual for the input truth table
		test.printNetwork();
	}
}