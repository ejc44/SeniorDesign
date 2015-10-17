/*
	Can generate a canonical SOP network
	
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
				input_nodes[i] = new Node(X1NOT+(i-num_inputs), false);
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

	// Disconnect two nodes
	private void disconnectNodes(Node parent, Node child) {
		(child.parents).remove(parent);	// Remove the parent from the child's parents list
		(parent.children).remove(child);	// Remove the child from the parent's children list 
	}
	
	
	// Update the cost variable
	private void calcCost()
	{
		// First remove any now unconnected nodes (excluding inputs and root)
		for (int i = 0; i < all_nodes.size(); i++)
		{
			Node curr = all_nodes.get(i);
			if (((curr.parents).size() == 0) && ((curr.children).size() == 0) && !curr.is_root && !curr.is_input) // If no children, parents, and not an input or root, remove it
			{
				all_nodes.remove(i);
			}	
		}	
	
		// Find cost
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
	
	
	public void print_tree()
	{
		int num_input_nodes = num_inputs*2+2;
	
		ArrayList<Node> level_children = new ArrayList<Node>();
		
		System.out.println("\nParent : Children\n");
		
		for (int i = 0; i< num_input_nodes; i++)
		{
			if(((all_nodes.get(i+1)).children).size() > 0) // If input has a child
			{
				print_gate(all_nodes.get(i+1)); // Print the input node
				System.out.print(": ");
				
				for (int j=0; j< ((all_nodes.get(i+1)).children).size(); j++) // Print all of its children
				{
					print_gate(((all_nodes.get(i+1)).children).get(j));
					if(level_children.contains(((all_nodes.get(i+1)).children).get(j)) == false)
					{
						level_children.add(((all_nodes.get(i+1)).children).get(j)); // Store its children
					}
				}
				System.out.println();
			}
		}
		
		while(level_children.size() > 0) // While are children
		{
			ArrayList<Node> new_children = new ArrayList<Node>();
			
			for (int i =0; i < level_children.size(); i++)
			{
				if (((level_children.get(i)).children).size() > 0)
				{
					print_gate((level_children.get(i))); // Print the input node
					System.out.print(": ");
				
				for (int j=0; j< ((level_children.get(i)).children).size(); j++) // Print all of its children
				{
					print_gate(((level_children.get(i)).children).get(j));
					if(new_children.contains(((level_children.get(i)).children).get(j)) == false)
					{
						new_children.add(((level_children.get(i)).children).get(j)); // Store its children
					}
				}
				System.out.println();
				
				}
			}
			
			level_children.clear();
			level_children = new ArrayList<Node>(new_children);
			new_children.clear();
		}
	
	}
	
	
	private void print_gate(Node desired_node)
	{
		switch(desired_node.gate_type) // Print the desired node node
		{
			case 0:
				System.out.print("X1 "); 
				break;
			case 1:
				System.out.print("X2 "); 
				break;
			case 2:
				System.out.print("X3 ");
				break;
			case 3:
				System.out.print("X4 "); 
				break;
			case 4:
				System.out.print("X5 "); 
				break;
			case 10:
				System.out.print("X1' ");
				break;
			case 11:
				System.out.print("X2' "); 
				break;
			case 12:
				System.out.print("X3' "); 
				break;
			case 13:
				System.out.print("X4' "); 
				break;
			case 14:
				System.out.print("X5' ");
				break;
			case 20:
				System.out.print("0 ");
				break;
			case 21:
				System.out.print("1 ");
				break;
			case 30:
				System.out.print("AND"+all_nodes.indexOf(desired_node)+" ");
				break;
			case 31:
				System.out.print("OR"+all_nodes.indexOf(desired_node)+" ");
				break;
			case 32:
				System.out.print("NAND"+all_nodes.indexOf(desired_node)+" ");
				break;
			case 33:
				System.out.print("NOR"+all_nodes.indexOf(desired_node)+" ");
				break;
			case 34:
				System.out.print("XOR"+all_nodes.indexOf(desired_node)+" ");
				break;
			case 35:
				System.out.print("XNOR"+all_nodes.indexOf(desired_node)+" ");
				break;
		}	
	}
	
	
	
	//---------------------------- End of completed work -----------------------------
	
	// Tells tree to mutate
	public void mutate()
	{
		boolean mutate_success = false;
		
		while (mutate_success != true)
		{
			double p = random_generator.nextDouble(); // Generate a probability
			
			// Choose type of mutation
			// Currently have probabilities hard-coded, but will probably be replaced with some sort of array with a generating function
			if (p < 0.2)
			{
				mutate_success = deleteInput();
			}
			else if (p < 0.4)
			{
				mutate_success = addInput();
			}
			else if (p < 0.5)
			{
				mutate_success = changeType();
			}
			else if (p < 0.6)
			{
				mutate_success = deleteGate();
			}
			else if (p < 0.8)
			{
				mutate_success = addGate();
			}
			else if (p < 0.9)
			{
				mutate_success = addConnection();
			}
			else
			{
				mutate_success = reassignInputs();
			}	
		}	
	}
	
	// Delete an input from the network
	private boolean deleteInput()
	{
		Node child = selectNode(false); // Choose the child
		int loops = 0;
		
		// Find a new child if the child has less than 3 inputs
		while((child.parents).size() < 3 && loops<5) {
			child = selectNode(false);
			loops++;
		}
		
		if ((child.parents).size() < 3) // Did not find a node with more than 2 inputs
		{
			return false;
		}

		// Select random input
		int rand = random_generator.nextInt((child.parents).size());
		Node input = (child.parents).get(rand);

		// Remove connection
		disconnectNodes(input,child);

		// Recalculate cost;
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;
	}
	
	// Add an input to the network
	private boolean addInput()
	{
		Node child = selectNode(false); // Choose the child
		Node input = selectNode(true);	// Choose the input
		int loops = 0;

		// Choose new input if it is already connected
		while((child.parents).contains(input) && loops<5) {
			input = selectNode(true);
			loops++;
		}
		
		if ((child.parents).contains(input)) // If still connected
		{
			return false;
		}

		// Connect input
		connectNodes(input,child);

		// Recalculate cost;
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;
	}
	
	// Change the gate type of a node
	private boolean changeType()
	{
		Node mutated = selectNode(false); // Choose the node to change
		int loops = 0;
		
		// Choose random new gate
		int new_gate = 30 + random_generator.nextInt(6);

		// Choose a different gate type if it's the current gate type
		while(mutated.gate_type == new_gate && loops < 5) {
			new_gate = 30 + random_generator.nextInt(6);
			loops++;
		}

		if (mutated.gate_type == new_gate) // If gate type did not change
		{
			return false;
		}
		
		// Change the gate type
		mutated.gate_type = new_gate;
		
		// Recalculate cost - currently unnecessary, but could change with different gate types
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;
	}
	
	// Remove a gate
	private boolean deleteGate()
	{
		Node removed = selectNode(false); // Choose the node to remove
		int loops = 0;
	
		// Find a new node if the random node is the root
		while(removed.is_root==true || loops<5) {
			removed = selectNode(false);
			loops++;
		}
		
		if(removed.is_root==true) // If still have selected root
		{
			return false;
		}


		// Connect all of the parents of 'removed' to all of the children of 'removed'
		for(int i=0;i<(removed.parents).size();i++) {
			for(int j=0;j<(removed.children).size();j++) {
				connectNodes((removed.parents).get(i),(removed.children).get(j));
			}
		}

		// From 'removed's parents, remove the connection to 'removed'
		for(int i=0;i<(removed.parents).size();i++) {
			Node par = (removed.parents).get(i);
			disconnectNodes(par, removed);
		}

		// From 'removed's children, remove the connection to 'removed'
		for(int i=0;i<(removed.children).size();i++) {
			Node child = (removed.children).get(i);
			disconnectNodes(removed, child);
		}

		// Remove the gate's node completely
		all_nodes.remove(removed);		
		
		// Recalculate cost
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;		
	}
	
	// Add a gate
	private boolean addGate()
	{
		Node child = selectNode(false); // Choose the child of the new node
		
		// Choose parent nodes (currently cannot be inputs)
		Node parent1 = selectNode(false);
		Node parent2 = selectNode(false);
		
		// Need to check for cyclic-ness
		boolean cyclic1 = isCyclic(parent1, child);
		boolean cyclic2 = isCyclic(parent2, child);
		int loop1 = 0;
		int loop2 = 0;
		if (cyclic1 && loop1 < 5) // Choose new parent if cyclic
		{
			parent1 = selectNode(false);
			cyclic1 = isCyclic(parent1, child);
			loop1++;
		}
		if (cyclic2 && loop2 < 5)
		{
			parent2 = selectNode(false);
			cyclic2 = isCyclic(parent2, child);
			loop2++;
		}
		if (cyclic1 || cyclic2)
		{
			return false;
		}
		
		Node new_node = new Node(30 + random_generator.nextInt(6),false); // Add a new gate
		
		// Connect nodes
		connectNodes(parent1, new_node);
		connectNodes(parent2, new_node);
		connectNodes(new_node, child);
		
		// Recalculate cost
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;		
	}

	// Add connection
	public boolean addConnection() {
		Node child = selectNode(false);
		Node newPar = selectNode(false);
		int loops = 0;

		// Need to check for cyclic-ness
		boolean cyclic = isCyclic(newPar, child);
		int loop = 0;
		if (cyclic && loop < 5) // Choose new parent if cyclic
		{
			newPar = selectNode(false);
			cyclic = isCyclic(newPar, child);
			loop++;
		}
		if (cyclic)
		{
			return false;
		}
		
		connectNodes(newPar,child);
		
		// Recalculate cost
		calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;		

	}
	
	// Reassign inputs
	private boolean reassignInputs()
	{
		/*// Generate an array list of all the inputs in use
		ArrayList<Node> inputs = new ArrayList<Node>();
		for(int i=0;i<all_nodes.size();i++) {
			if((all_nodes.get(i)).is_input == true) {
				inputs.add(all_nodes.get(i));
			}
		}*/
		
		// Rassign inputs in cyclic order
		for(int i=0;i<all_nodes.size();i++) 
		{
			Node curr_node = all_nodes.get(i);
			if(curr_node.is_input == true) 
			{
				if((curr_node.gate_type < 3) || (curr_node.gate_type < 13 && curr_node.gate_type > 4) || (curr_node.gate_type == 20))
				{
					curr_node.gate_type += 1;
				}
				else if (curr_node.gate_type == 4 || curr_node.gate_type == 14)
				{
					curr_node.gate_type += 6;
				}
				else
				{
					curr_node.gate_type = 0;
				}
			}
		}
		
		// Recalculate cost -- unnecessary
		//calcCost();
		
		// Update the truth table
		updateTable();
		
		return true;		
	}
	
	
	// Selects Node to mutate
	private Node selectNode(boolean isInput)
	{
		int s = all_nodes.size();

		// Select a random node
		int rand = random_generator.nextInt(s);
		Node selected = all_nodes.get(rand);

		// Check if the random node matches the given isInput value
		// If it doesn't match, find a new node
		while(selected.is_input != isInput) {
			rand = random_generator.nextInt(s);
			selected = all_nodes.get(rand);
		}

		return selected;
	}
	
	// Check if two nodes trying to connect will be cyclically connected
	private boolean isCyclic(Node par, Node child)
	{
		
		ArrayList<Node> descendants = new ArrayList<Node>(); // All of child's descendants
		
		for (int i = 0; i < (child.children).size(); i++)
		{
			Node curr = (child.children).get(i);
			if (curr == par) // Break if cyclic
			{
				return true;
			}
			else
			{
				descendants.add(curr);
			}
		}
	
		
		while(descendants.size() > 0) // While are descendants
		{
			ArrayList<Node> new_children = new ArrayList<Node>();
			
			for (int i =0; i < descendants.size(); i++)
			{
				Node curr = descendants.get(i);
				
				if (curr == par) // Break if cyclic
				{
					return true;
				}
				else
				{
					new_children.add(curr);
				}
			}
			
			descendants.clear();
			descendants = new ArrayList<Node>(new_children);
			new_children.clear();
		}
		
		return false;
	}

	private void updateTable()
	{
	
	}












	public String printNetwork() {
		return print_tree_again(findRoot(),"",0);
	}

	private Node findRoot() {
		Node root = (all_nodes).get(0);
		for(int i=0;i<all_nodes.size();i++) {
			if((all_nodes.get(i)).is_root == true) {
				root = all_nodes.get(i);
				break;
			}
		}
		return root;
	}

	private String print_tree_again(Node child, String s, int level) {
		if(child.is_root == true) {
			s = s+"f = ";
		}

		for(int i=0;i<level;i++) {
			s = s+"\t";
		}
		s = s+gate_string(child);
		
		if(child.is_input == false) {
			s = s+"(\n";
		}
		
		ArrayList<Node> parents = child.parents;
		
		for(int i=0;i<parents.size();i++) {
			s = s+print_tree_again(parents.get(i),"",level+1);
			
			if(parents.size()>1 && i<(parents.size()-1)) {
				s = s+",\n";
			}
		}
		
		if(child.is_input == false) {
			s = s+")";
		}
		
		return s;
	}

	private String gate_string(Node n) {
		String gate_type="";
		
		switch(n.gate_type) { 
			case 0:
				gate_type = "X1"; 
				break;
			case 1:
				gate_type = "X2"; 
				break;
			case 2:
				gate_type = "X3";
				break;
			case 3:
				gate_type = "X4"; 
				break;
			case 4:
				gate_type = "X5"; 
				break;
			case 10:
				gate_type = "X1'";
				break;
			case 11:
				gate_type = "X2'"; 
				break;
			case 12:
				gate_type = "X3'"; 
				break;
			case 13:
				gate_type = "X4'"; 
				break;
			case 14:
				gate_type = "X5'";
				break;
			case 20:
				gate_type = "0";
				break;
			case 21:
				gate_type = "1";
				break;
			case 30:
				gate_type = "AND"+all_nodes.indexOf(n);
				break;
			case 31:
				gate_type = "OR"+all_nodes.indexOf(n);
				break;
			case 32:
				gate_type = "NAND"+all_nodes.indexOf(n);
				break;
			case 33:
				gate_type = "NOR"+all_nodes.indexOf(n);
				break;
			case 34:
				gate_type = "XOR"+all_nodes.indexOf(n);
				break;
			case 35:
				gate_type = "XNOR"+all_nodes.indexOf(n);
				break;
		}	
		
		return gate_type;
	
	
	}

}