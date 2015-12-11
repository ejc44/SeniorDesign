# Senior Project: Evolutionary Algorithm for Boolean Logic Minimization

This program is able to generate boolean logic networks for user-supplied truth tables.
An evolutionary alogirthm is utilized to minimize the logic networks. Additionallty, networks are stored in a database to make retrieving a network fast. 

## Running the Program:
To run this program, you must have Java JDK or JRE installed. To install Java JDK: http://www.oracle.com/technetwork/java/javase/downloads/index.html
### Truth table file formats
1. txt file
	* Contains the final output of the truth table you would like to retrieve. The first line of the file should be the output of the truth table where the inputs are all 0, and the final line of the file should be the output of the truth table where all of the inputs are 1. The 4var2.txt file can be viewed for an example of this format.
2. csv file
	* Contains the entire truth table and the final output of the truth table you would like the retrieve. The first column of the csv file represents the first input, and the following columns represent each input. After all the input columns have been created, the final column represents the desired output. The 3var1full.csv file can be viewed for an example of this format.
	* Contains only the final output of the truth table you would like to retrieve. The first column of the file represents the final output. The first row should be the output of the truth table where the inputs are all 0, and the final row of the file should be the output of the truth table where all of the inputs are 1. The 3var1.csv or 5var1.csv files can be viewed for an example of this format.

Evolutionary Algorithm for Boolean Logic Minimization by Gabrielle Clark & Emily Crabb is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License.