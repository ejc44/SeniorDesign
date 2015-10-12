/*
	Can generate a canonical SOP network
	Cannot yet actually mutate anything
	Need to figure out how to print network so can check results better
*/

import java.util.*;

public class BooleanTree {
	
	private int num_inputs; // Number of possible inputs (max is 5)
	private int[] truth_table; // Truth table for the tree
	private int cost; // Cost of the current truth table = numbers of nodes + number of inputs to each node (excluding inputs)
	private Random random_generator; // Random number generator object
	private ArrayList<Node> all_nodes; // All nodes in the tree

	// Gate representations as integers
	// Variables
	private static final int X1 = 0;
	private static final int X2 = 1;
	private static final int X3 = 2;
	private static final int X4 = 3;
	private static final int X5 = 4;
	// Inverted variables
	private static final int X1NOT = 10;
	private static final int X2NOT = 11;
	private static final int X3NOT = 12;
	private static final int X4NOT = 13;
	private static final int X5NOT = 14;
	// 0 and 1
	private static final int LOW = 20;
	private static final int HIGH = 21;
	// Boolean logic gates
	private static final int AND = 30;
	private static final int OR = 31;
	private static final int NAND = 32;
	private static final int NOR = 33;
	private static final int XOR = 34;
	private static final int XNOR = 35;
	
	
	
	// Node class
	private class Node{
		private int gate_type; // Integer representation of the type of gate
		private boolean is_root; // Cannot remove root node
		private boolean is_input; // Special properties
		private ArrayList<Node> parents; // Node array of the parents
		private ArrayList<Node> children; // Node array of its children
		//private int[] truth_value; // Truth table value up to that point	
		
		// Constructor
		private Node(int gt, boolean ir)
		{
			parents = new ArrayList<Node>();
			children = new ArrayList<Node>();
			gate_type = gt;
			is_root = ir;
			if (gate_type < AND)
			{
				is_input = true;
			}
			else
			{
				is_input = false;
			}
		}

	}

	
	// Generator
	// Pass in the number of variables, the truth table, the sum of products table, and the number of sum of products terms
	public BooleanTree(int num_vars, int[] table, int[][] SOP, int sop_terms)
	{
		random_generator = new Random(System.currentTimeMillis()); // Seed random number generator
		all_nodes = new ArrayList<Node>();
		
		num_inputs = num_vars;
		truth_table = table.clone(); // Copy the truth table into the tree
		
		
		// Make the tree from the SOP form
		
		// First add the root - always an OR gate
		Node root =  new Node(OR, true);
		all_nodes.add(root);
		
		// Next add the input nodes - add 1 of each possible input
		int num_input_nodes = num_inputs*2 + 2; // Each variable, each inverted variable, and 0 and 1
		Node[] input_nodes = new Node[num_input_nodes];
		
		for (int i = 0; i < num_input_nodes; i++) // Add the input nodes
		{
			if (i < num_inputs) // Regular variables
			{
				input_nodes[i] = new Node(X1+i, false);
			}
			else if (i < num_inputs*2) // Inverted variables
			{
				input_nodes[i] = new Node(X1NOT+i, false);
			}
			else if (i == num_inputs*2) // Low
			{
				input_nodes[i] = new Node(LOW, false);
			}
			else // High
			{
				input_nodes[i] = new Node(HIGH, false);
			}
			all_nodes.add(input_nodes[i]);
		}
		
		
		// Last add AND nodes and connect
		for(int i = 0; i < sop_terms; i++) // For each SOP term
		{
			Node and_node = new Node(AND, false); // Add AND node
			all_nodes.add(and_node);
			
			for(int var = 0; var < num_vars; var++) // Connect to inputs correctly
			{
				if (SOP[i][var] == 0) // Should be inverted
				{
					connectNodes(input_nodes[var+num_vars], and_node);
				}
				else // Should not be inverted
				{
					connectNodes(input_nodes[var], and_node);
				}
			}
			
			connectNodes(and_node,root); // Connect to root
		}
		
		
		calcCost(); // Get the cost of the new tree
		
		
		//System.out.println("The SOP terms is " + sop_terms);
		//System.out.println("The num nodes is " + all_nodes.size());
		//System.out.println("The cost is " + cost);
	}
	
	// Connect two nodes
	private void connectNodes(Node parent, Node child)
	{
		(parent.children).add(child); // Add the child to the parent's children list
		(child.parents).add(parent); // Add the parent to the child's children list
	}
	
	
	// Update the cost variable
	private void calcCost()
	{
		cost = all_nodes.size(); // Number of nodes
		
		for (int i = 0; i < all_nodes.size(); i++)
		{
			Node node_i = all_nodes.get(i); // Iterate over each node in array list
			if (node_i.is_input)
			{
				cost = cost - 1; // Remove from number of nodes
			}
			else
			{
				cost += (node_i.parents).size(); // Add number of inputs of each node
			}
		}
	}
	
	// Returns the cost
	public int getCost()
	{
		return cost;
	}
	
	
	//---------------------------- End of completed work -----------------------------
	
	// Tells tree to mutate
	public void mutate()
	{
		double p = random_generator.nextDouble(); // Generate a probability
		
		// Choose type of mutation
		// Currently have probabilities hard-coded, but will probably be replaced with some sort of array with a generating function
		if (p < 0.2)
		{
			deleteInput();
		}
		else if (p < 0.4)
		{
			addInput();
		}
		else if (p < 0.5)
		{
			changeType();
		}
		else if (p < 0.6)
		{
			deleteGate();
		}
		else if (p < 0.8)
		{
			addGate();
		}
		else
		{
			reassignInputs();
		}
		
	}
	
	// Delete an input from the network
	private void deleteInput()
	{
		Node removed = selectNode(true); // Choose the node to remove
	
	}
	
	// Add an input to the network
	private void addInput()
	{
		Node child = selectNode(false); // Choose the child
	
	}
	
	// Change the gate type of a node
	private void changeType()
	{
		Node mutated = selectNode(false); // Choose the node to change
	
	}
	
	// Remove a gate
	private void deleteGate()
	{
		Node removed = selectNode(false); // Choose the node to remove
	
	}
	
	// Add a gate
	private void addGate()
	{
		Node child = selectNode(false); // Choose the child of the new node
	
	}
	
	// Reassign inputs
	private void reassignInputs()
	{
		
	
	}
	
	
	// Selects Node to mutate
	private Node selectNode(boolean isInput)
	{
	
		return null;
	}
	


}