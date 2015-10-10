import java.io.*;
import java.lang.Math;

public class readFile {
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

					if(Integer.parseInt(hold[numVars]) == 1) {
						for(int j=0;j<numVars;j++) {
							sumOfProducts[i][j] = Integer.parseInt(hold[j]);
						}
						i++;
						sopTerms++;
					}
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
	}
}