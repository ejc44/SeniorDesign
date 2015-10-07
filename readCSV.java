import java.io.*;
import java.lang.Math;

public class readCSV {
	public static void main(String [] args) throws IOException {
		String filename=" ";
		int numVars = 0;
		
		numVars = Integer.parseInt(args[0]);
		filename= args[1];
		
		BufferedReader buff=null;
		
		int[] table = new int[(int) (Math.pow(2,numVars))];
		
		try {
			buff=new BufferedReader (new FileReader(filename));
			String line;
			int i=0;
			
			while((line=buff.readLine()) != null) {
				table[i]=Integer.parseInt(line);
				i++;
			}
		} catch (IOException e) {
			System.out.println("Error with buffer");
		}
		
		int[][] sumOfProducts = new int[(int) (Math.pow(2,numVars))][numVars];
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
			}
		}
	}
}