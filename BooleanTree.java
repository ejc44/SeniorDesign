/*
Evolutionary Algorithm for Boolean Logic Minimization by Gabrielle Clark & Emily Crabb is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License.
Based on a work at https://github.com/ejc44/SeniorDesign.
*/


/*
	Boolean Tree file
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
		private int node_level; // Number of levels between inputs and node (node_level = 0 for inputs)
		
		// Constructor
		private Node(int gt, boolean ir)
		{
			parents = new ArrayList<Node>();
			children = new ArrayList<Node>();
			gate_type = gt;
			is_root = ir;
			node_level = -1; // Invalid value by default
			
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
		
		calcNodeLevels();
		
		calcCost(); // Get the cost of the new tree
		
		updateTable(); // Update the table
	}



	public BooleanTree(int numVars, ArrayList<String> lines) {
		random_generator = new Random(System.currentTimeMillis()); // Seed random number generator
		all_nodes = new ArrayList<Node>();
		
		num_inputs = numVars;
		truth_table = new int[(int) (Math.pow(2,numVars))];

		ArrayList<String> gates = new ArrayList<String>();

		// Create all the nodes
		for(int i=0;i<lines.size();i++) {
			// Split the lines into child & parent
			String[] hold = (lines.get(i)).split(":");
			String child = hold[0];

			// Add the string version of child to the gates arraylist
			gates.add(child);

			// Using the string, find the integer gate value
			int gate = stringToGate(child);

			// Root node will be the first line of the file
			Boolean root = false;
			if(i==0) {root=true;};

			// Create the node
			Node n = new Node(gate,root);

			// Add node to all_nodes
			all_nodes.add(n);
		}

		// Connect the child and parent nodes		
		for(int i=0;i<lines.size();i++) {
			// Split line into child and parents
			String [] hold = (lines.get(i)).split(":");
			// Check if there are any parents
			if(hold.length>1) {
				String parents = hold[1];

				// Find the child node
				Node childNode = all_nodes.get(i);

				// Split up the parents into separate strings
				String[] parent = parents.split(" ");

				// For every parent string, find it's index, and then it's node, then connect the child and parent
				for(int j=0;j<parent.length;j++) {
					int parentNodeIndex = gates.indexOf(parent[j]);
					Node parentNode = all_nodes.get(parentNodeIndex);

					connectNodes(parentNode,childNode);
				}
			}
		}

		// Check if low input node exists
		Boolean found = false;
		for(int i=0;i<all_nodes.size();i++) {
			Node n = all_nodes.get(i);

			if(n.gate_type==LOW) {
				found=true;
				break;
			}
		}
		// Add low input if it doesn't already exist
		if(found==false) {
			Node n = new Node(LOW,false);
		}


		// Check if high input node exists
		found=false;
		for(int i=0;i<all_nodes.size();i++) {
			Node n = all_nodes.get(i);

			if(n.gate_type==HIGH) {
				found=true;
				break;
			}
		}
		// Add high input if it doesn't already exist
		if(found==false) {
			Node n = new Node(HIGH,false);
		}

		calcNodeLevels();
		
		calcCost();
		
		updateTable(); // Update the table
	}

	// Find the node levels for all nodes in the network
	private void calcNodeLevels()
	{
		int curr_level = 0;
		
		ArrayList<Node> level_children = new ArrayList<Node>();
		
		for(int i =0; i < all_nodes.size(); i++) // Initialize node_level and find input nodes
		{
			Node curr = all_nodes.get(i);
			curr.node_level = 0;// Set all levels to zero by default
			if (curr.is_input) // Collect all input nodes
			{
				level_children.add(curr);
			}
		}
			
		while(level_children.size() > 0) // While are children
		{
			ArrayList<Node> new_children = new ArrayList<Node>();
			
			for (int i =0; i < level_children.size(); i++) // For every node in level_children
			{
				Node curr_node = level_children.get(i);
				
				for (int j=0; j< (curr_node.children).size(); j++) //Increment level of all of its children
				{
					Node child_node = (curr_node.children).get(j);
					
					if(new_children.contains(child_node) == false) // Hasn't already been added and incremented
					{
						new_children.add(child_node); // Store its parents
						child_node.node_level += 1; // Increment its level
					}
				}				
			}
			
			level_children.clear();
			level_children = new ArrayList<Node>(new_children);
			new_children.clear();
		}
	}
	

	
	// Connect two nodes
	private void connectNodes(Node parent, Node child)
	{
		if (!(parent.children).contains(child)) // If not already in list
		{
			(parent.children).add(child); // Add the child to the parent's children list
		}
		if (!(child.parents).contains(parent)) // If not already in list
		{
			(child.parents).add(parent); // Add the parent to the child's children list
		}
	}

	// Disconnect two nodes
	private void disconnectNodes(Node parent, Node child) 
	{
		while ((child.parents).contains(parent)) // Remove all instances
		{
			(child.parents).remove(parent);	// Remove the parent from the child's parents list
		}
		while ((parent.children).contains(child)) // Remove all instances
		{
			(parent.children).remove(child);	// Remove the child from the parent's children list 
		}
	}
	
	
	// Update the cost variable
	private void calcCost()
	{
		// First remove any now unconnected nodes (excluding inputs and root)
		boolean removed = true;
		while (removed) // Need while loop so can completely remove any orphaned sub-trees
		{
			removed = false;
			for (int i = 0; i < all_nodes.size(); i++) // Check every node
			{
				Node curr = all_nodes.get(i);
				if (!curr.is_root && !curr.is_input && (((curr.children).size() == 0) || ((curr.parents).size() == 0))) // If no children and not an input or root, remove it
				{
					for (int j = 0; j < (curr.parents).size(); j++) // Disconnect from its parents
					{
						disconnectNodes((curr.parents).get(j), curr);
					}
					
					all_nodes.remove(i);
					removed = true;
				}	
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


	public String fileOutput() {
		String s = print_tree();
		return s;
	}

	private String print_tree() {
		String s = "";
		String hold;
		
		// Get root node & its index
		Node n = findRoot();
		int rootIndex = all_nodes.indexOf(n);
		
		// Add root gate to string

		s += gateToString(n)+":";
		
		// Add parents of root to string
		for(int i=0;i<(n.parents).size();i++) {
			s += gateToString((n.parents).get(i))+" ";
		}
		s += "\n";
		
		if (cost == 0) // Only print input node if cost 0
		{
			return s;
		}
		
		// Loop through all the nodes, and output child:parents
		for(int i=0;i<all_nodes.size();i++) {
			if(i!=rootIndex) {	// Check if the index values is the same as the root. We don't want it to output twice
				// Print child node
				s += gateToString(all_nodes.get(i))+":";
				
				// Print parents
				for(int j=0;j<((all_nodes.get(i)).parents).size();j++) {
					s += gateToString(((all_nodes.get(i)).parents).get(j)) + " ";
				}
				s += "\n";
			}
		}
		
		return s;
	}
	
	public String tree_string()
	{
		String s = "";
		
		ArrayList<Node> level_parents = new ArrayList<Node>();
		
		Node curr_node = all_nodes.get(0); // Extract root
				
		if((curr_node.parents).size() > 0) // If root has parent 
		{
			s = s + gateToString(curr_node)+" "; // Print the root node
			s = s + ": ";
			
			for (int j=0; j< (curr_node.parents).size(); j++) // Print all of its parents
			{
				Node par_node = (curr_node.parents).get(j);
				s = s + gateToString(par_node)+" ";
				if(level_parents.contains(par_node) == false)
				{
					level_parents.add(par_node); // Store its parents
				}
			}
			s = s + '\n';
		}
				
		while(level_parents.size() > 0) // While are parents
		{
			ArrayList<Node> new_parents = new ArrayList<Node>();
			
			for (int i =0; i < level_parents.size(); i++)
			{
				curr_node = level_parents.get(i);
				
				if ((curr_node.parents).size() > 0)
				{
					s = s + gateToString(curr_node)+" "; // Print the input node
					s = s + ": ";
				
				for (int j=0; j< (curr_node.parents).size(); j++) // Print all of its parents
				{
					Node par_node = (curr_node.parents).get(j);
					s = s + gateToString(par_node)+" ";
					if(new_parents.contains(par_node) == false)
					{
						new_parents.add(par_node); // Store its parents
					}
				}
				s = s + '\n';				
				}
			}
			
			level_parents.clear();
			level_parents = new ArrayList<Node>(new_parents);
			new_parents.clear();
		}
	
		return s;
	}
	
	
	// Tells tree to mutate
	public void mutate()
	{
		boolean mutate_success = false; // Whether mutation succeeded
		int attempts = 0; // Number of mutation attempts - so doesn't get stuck if only root node exists
		
		// Do not allow mutations if total cost 0 (just an input node) or if gets stuck (over 100 unsuccessful attempts)
		while (mutate_success != true && attempts<100 && cost > 0)
		{
			double p = random_generator.nextDouble(); // Generate a probability
			
			// Choose type of mutation			
			int sop_cost = calcSOPCost(); // SOP Cost = maximum possible cost
			
			// Probabilities of each mutation
			double delete_connection_prob = 3*cost; // More probable if high cost
			if(delete_connection_prob < 0)
			{
				delete_connection_prob = 0;
			}
			double add_input_prob = 2*(sop_cost - cost); // More probable if low cost
			if(add_input_prob < 0)
			{
				add_input_prob = 0;
			}
			double change_type_prob = 3*(sop_cost - cost); // More probable if low cost
			if(change_type_prob < 0)
			{
				change_type_prob = 0;
			}
			double delete_gate_prob = 2*(cost); // More probable if high cost
			if(delete_gate_prob < 0)
			{
				delete_gate_prob = 0;
			}
			double add_gate_prob = sop_cost - cost; // More probable if low cost
			if(add_gate_prob < 0)
			{
				add_gate_prob = 0;
			}
			double add_connection_prob = 2*(sop_cost - cost); // More probable if low cost
			if(add_connection_prob < 0)
			{
				add_connection_prob = 0;
			}
			double reassign_input_prob = 4*(sop_cost - cost); // More probable if low cost
			if(reassign_input_prob < 0)
			{
				reassign_input_prob = 0;
			}
			double delete_root_prob = cost; // More probable if high cost
			if(delete_root_prob < 0)
			{
				delete_root_prob = 0;
			}
			
			
			double total = delete_connection_prob + add_input_prob + change_type_prob + delete_gate_prob + add_gate_prob + add_connection_prob + reassign_input_prob + delete_root_prob;
			
			delete_connection_prob = delete_connection_prob / total;
			add_input_prob = add_input_prob / total + delete_connection_prob;
			change_type_prob = change_type_prob / total + add_input_prob;
			delete_gate_prob = delete_gate_prob / total + change_type_prob;
			add_gate_prob = add_gate_prob / total + delete_gate_prob;
			add_connection_prob = add_connection_prob / total + add_gate_prob;
			reassign_input_prob = reassign_input_prob / total + add_connection_prob;
			delete_root_prob = delete_root_prob / total + reassign_input_prob;
			
			
			if (p < delete_connection_prob)
			{
				mutate_success = deleteConenction();
			}
			else if (p < add_input_prob)
			{
				mutate_success = addInput();
			}
			else if (p < change_type_prob)
			{
				mutate_success = changeType();
			}
			else if (p < delete_gate_prob)
			{
				mutate_success = deleteGate();
			}
			else if (p < add_gate_prob)
			{
				mutate_success = addGate();
			}
			else if (p < add_connection_prob)
			{
				mutate_success = addConnection();
			}
			else if (p < reassign_input_prob)
			{
				mutate_success = reassignInputs();
			}
			else
			{
				mutate_success = deleteRoot();
			}
			attempts++;			
		}	
	}
	
	// Calculate the sum-of-products cost
	private int calcSOPCost() 
	{
		int cost = 0;
		int andGates = 0;
		for(int i=0;i<truth_table.length;i++) {
			if(truth_table[i]==1) {
				cost += num_inputs + 1;
				andGates++;
			}
		}
 
		cost += andGates + 1;

		return cost;
	}
	
	
	// Delete connection between two nodes
	private boolean deleteConenction()
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
		
		// Update node levels
		calcNodeLevels();
		
		// Update the truth table
		updateTable();
		
		return true;
	}
	
	// Add an input to a node
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
		
		// Update node levels
		calcNodeLevels();
	
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
		int new_gate = AND + random_generator.nextInt(6);

		// Choose a different gate type if it's the current gate type
		while(mutated.gate_type == new_gate && loops < 5) {
			new_gate = AND + random_generator.nextInt(6);
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
		
		// Update node levels - should not change anything
		calcNodeLevels();		
		
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
		while(removed.is_root==true && loops<5) {
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
		
		// From removed's parents, remove the connection to 'removed'
		while(!(removed.parents).isEmpty()) 
		{
			Node par = (removed.parents).get(0);
			disconnectNodes(par, removed);
			if (!par.is_input && ((par.parents).size() < 2)) // In case fewer than two parents, need to add an additional parent
			{
				boolean success = false;
				while (!success) // Add an input node as a parent but cannot already be a parent
				{
					Node newpar = selectNode(true);
					if (!(par.parents).contains(newpar))
					{
						connectNodes(newpar, par);
						success = true;
					}
				}
			}
		}

		// From removed's children, remove the connection to 'removed'
		while(!(removed.children).isEmpty()) 
		{
			Node child = (removed.children).get(0);
			disconnectNodes(removed, child);
		}

		// Remove the gate's node completely
		while (all_nodes.contains(removed)) // Remove all instances
		{
			all_nodes.remove(removed);		
		}
		
		// Recalculate cost
		calcCost();
		
		// Update node levels
		calcNodeLevels();
		
		// Update the truth table
		updateTable();
		
		return true;		
	}
	
	// Add a gate
	private boolean addGate()
	{
		Node child = selectNode(false); // Choose the child of the new node
		Node new_node = new Node(AND + random_generator.nextInt(6),false); // Add a new gate
		double prob = 0.3; // Probability of using input as parent
		
		// Choose parent nodes
		double p1 = random_generator.nextDouble(); // Generate a probability
		double p2 = random_generator.nextDouble(); // Generate a probability
		Node parent1;
		Node parent2;
		if (p1 < prob)
		{
			parent1 = selectNode(true);
		}
		else
		{
			parent1 = selectNode(false);
		}
		if (p2 < prob)
		{
			parent2 = selectNode(true);
		}
		else
		{
			parent2 = selectNode(false);
		}
		
		// Need to check for cyclic-ness
		boolean cyclic1 = isCyclic(parent1, child);
		int loop1 = 0;
		while (cyclic1 && loop1 < 5) // Choose new parent if cyclic
		{
			p1 = random_generator.nextDouble(); // Generate a probability
			if (p1 < prob)
			{
				parent1 = selectNode(true);
			}
			else
			{
				parent1 = selectNode(false);
			}
			cyclic1 = isCyclic(parent1, child);
			loop1++;
		}
		if (cyclic1)
		{
			return false;
		}
		all_nodes.add(new_node);
		connectNodes(parent1, new_node);
		connectNodes(new_node, child);
		
		// Update node levels
		calcNodeLevels();
		
		// Now check for cyclic-ness of other parent
		boolean cyclic2 = isCyclic(parent2, child);
		int loop2 = 0;
		
		while ((cyclic2 && loop2 < 5) || (parent1 == parent2))
		{
			p2 = random_generator.nextDouble(); // Generate a probability
			if (p2 < prob)
			{
				parent2 = selectNode(true);
			}
			else
			{
				parent2 = selectNode(false);
			}
			cyclic2 = isCyclic(parent2, new_node);
			loop2++;
		}
		if (cyclic2 || (parent1 == parent2))
		{
			disconnectNodes(parent1,new_node);
			disconnectNodes(new_node,child);
			while (all_nodes.contains(new_node)) // Remove all instances
			{
				all_nodes.remove(new_node);
			}
			calcCost();
			calcNodeLevels();// Update node levels
			updateTable();
			return false;
		}
		else
		{		
			connectNodes(parent2, new_node);
		}
		
		// Recalculate cost
		calcCost();
		
		// Update node levels
		calcNodeLevels();		
		
		// Update the truth table
		updateTable();
		
		return true;		
	}

	// Add connection
	public boolean addConnection() 
	{
		// Cannot connect to input - would be add input function
		Node child = selectNode(false);
		Node newPar = selectNode(false);
		int loops = 0;

		// Need to check for cyclic-ness
		boolean cyclic = isCyclic(newPar, child);
		int loop = 0;
		while (cyclic && loop < 5) // Choose new parent if cyclic
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
		
		// Update node levels
		calcNodeLevels();
		
		// Update the truth table
		updateTable();
		
		return true;		

	}
	
	// Reassign inputs
	private boolean reassignInputs()
	{
		// Reassign inputs in cyclic order
		for(int i=0;i<all_nodes.size();i++) 
		{
			Node curr_node = all_nodes.get(i);
			if(curr_node.is_input == true) 
			{
				if (num_inputs == 5)
				{
					if((curr_node.gate_type < 4) || (curr_node.gate_type>=10 && curr_node.gate_type<=13) || (curr_node.gate_type==20))
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
				else if (num_inputs == 4)
				{
					if((curr_node.gate_type < 3) || (curr_node.gate_type>=10 && curr_node.gate_type<13) || (curr_node.gate_type==20))
					{
						curr_node.gate_type += 1;
					}
					else if (curr_node.gate_type == 3 || curr_node.gate_type == 13)
					{
						curr_node.gate_type += 7;
					}
					else
					{
						curr_node.gate_type = 0;
					}
				}
				else
				{	
					if((curr_node.gate_type < 2) || (curr_node.gate_type>=10 && curr_node.gate_type<12) || (curr_node.gate_type==20))
					{
						curr_node.gate_type += 1;
					}
					else if (curr_node.gate_type == 2 || curr_node.gate_type == 12)
					{
						curr_node.gate_type += 8;
					}
					else
					{
						curr_node.gate_type = 0;
					}
					
				}
			}
		}
		
		// Recalculate cost
		calcCost();
		
		// Update node levels
		calcNodeLevels();
		
		// Update the truth table
		updateTable();
		
		return true;		
	}
	
	
	// Delete the root node and select a new root node
	private boolean deleteRoot()
	{
		Node root = findRoot(); // Get the root node

		int parSize = (root.parents).size();
		if(parSize>0) {
			int p = random_generator.nextInt(parSize);

			Node new_root = (root.parents).get(p);
			new_root.is_root = true;
			
			int num_children = (new_root.children).size();
			for (int i = 0 ; i < num_children; i++) // Disconnect the new root from all of its children
			{
				Node child = (new_root.children).get(0);
				disconnectNodes(new_root,child);
				if (!child.is_input && ((child.parents).size() < 2)) // If node now has fewer than two parents
				{
					boolean success = false;
					while (!success) // Add an input node as a parent but cannot already be a parent
					{
						Node newpar = selectNode(true);
						if (!(child.parents).contains(newpar))
						{
							connectNodes(newpar, child);
							success = true;
						}
					}
				}
			}
			
			// Disconnect the root from all its parents
			int num_parents = (root.parents).size();
			for (int i = 0; i < num_parents; i++)
			{
				disconnectNodes((root.parents).get(0), root);
			}
			root.is_root = false;
			
			boolean contains = true;
			
			while (contains)
			{
				contains = all_nodes.remove(root);
			}
					
			// Recalculate cost
			calcCost();
			
			// Update node levels
			calcNodeLevels();
			
			// Update the truth table
			updateTable();

			return true;
		} else {
			return false;
		}
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
		if (par.node_level < child.node_level) // Cyclic-ness depends on node level only
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private void updateTable()
	{
		int num_entries = (int)Math.pow(2,num_inputs);// Number of entries in the truth table
		
		for (int index = 0; index < num_entries; index++)
		{
			int expression = Integer.valueOf(Integer.toBinaryString(index)); // Number representing binary combination of inputs
			
			boolean[] input_values = new boolean[num_inputs];
			
			for (int i =0; i<num_inputs; i++) // Set input values (X1 is MSB)
			{
				int n = (index >> i) & 1;
				if (n== 1)
				{
					input_values[num_inputs-i-1] = true;
				}
				else
				{
					input_values[num_inputs-i-1] = false;
				}
			}			
			
			boolean value = evaluateNetwork(input_values, findRoot()); // New truth table value for given entry
			if (value)
			{
				truth_table[index] = 1;
			}
			else
			{
				truth_table[index] = 0;
			}
		}
	}
	
	private boolean evaluateNetwork(boolean[] input_values, Node curr)
	{
		boolean value = false;
	
		if (curr.is_input)
		{
			switch(curr.gate_type) // Print the desired node node
			{
				case X1: //X1
					value = input_values[0];
					break;
				case X2: //X2
					value = input_values[1];
					break;
				case X3: //X3
					value = input_values[2];
					break;
				case X4: //X4
					value = input_values[3];
					break;
				case X5: //X5
					value = input_values[4];
					break;
				case X1NOT: //X1'
					value = !input_values[0];
					break;
				case X2NOT: //X2'
					value = !input_values[1];
					break;
				case X3NOT: //X3'
					value = !input_values[2];
					break;
				case X4NOT: //X4'
					value = !input_values[3];
					break;
				case X5NOT: //X5'
					value = !input_values[4];
					break;
				case LOW: //Low
					value = false;
					break;
				case HIGH: //High
					value = true;
					break;
			}
		} 
		else
		{
			int num_true;
			switch(curr.gate_type) // Print the desired node node
			{
				case AND: // AND
					value = true;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						value = value && evaluateNetwork(input_values, (curr.parents).get(i));
					}
					break;
				case OR: // OR
					value = false;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						value = value || evaluateNetwork(input_values, (curr.parents).get(i));
					}
					break;
				case NAND: //NAND
					value = true;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						value = value && evaluateNetwork(input_values, (curr.parents).get(i));
					}
					value = !value;
					break;
				case NOR: // NOR
					value = false;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						value = value || evaluateNetwork(input_values, (curr.parents).get(i));
					}
					value = !value;
					break;
				case XOR:
					num_true  = 0;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						if(evaluateNetwork(input_values, (curr.parents).get(i)))
						{
							num_true++;
						}
					}
					if (num_true%2 == 1)
					{
						value = true;
					}
					else
					{
						value = false;
					}
					break;
				case XNOR: // XNOR
					num_true  = 0;
					for (int i =0; i < (curr.parents).size(); i++)
					{
						if(evaluateNetwork(input_values, (curr.parents).get(i)))
						{
							num_true++;
						}
					}
					if (num_true%2 == 1)
					{
						value = false;
					}
					else
					{
						value = true;
					}
					break;
				}
			}
		
		return value;
	}

	/********************
	Calls the print_tree_again funtion to get a string representation of the tree

	Return: a string representation of the tree network
	********************/
	public String printNetwork() {
		return print_tree_again(findRoot(),"",0);
	}

	/********************
	Loops through all the nodes in the network, and determines which is set as the root node

	Return: a Node which has it's is_root value set to true
	********************/
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

	/********************
	Creates the text version of the tree which will be ouput to the user.
	This is a recursive function.

	Node child: A child node
	String s: The string to be appended to
	int level: The level of recursion (used for adding tabs to the string for ease of understanding the output)

	Return: a string which represents the network tree
	********************/
	private String print_tree_again(Node child, String s, int level) {
		// If the child node is the root, create the string 
		if(child.is_root == true) {
			s = s+"f = ";
		}

		// Adppend the number of tabs necessary for the current level
		for(int i=0;i<level;i++) {
			s = s+"\t";
		}
		// Append the gate to the string
		s = s+gateToString(child);
		
		// Append an open parenthesis if there are parent nodes
		if(child.is_input == false) {
			s = s+"(\n";
		}
		
		ArrayList<Node> parents = child.parents;
		
		// For each parent node, call this funtion with the parent as the child node, a blank string, and 1 added to the level value
		for(int i=0;i<parents.size();i++) {
			s = s+print_tree_again(parents.get(i),"",level+1);
			
			// If there is more than one parent, add a comma after each parent
			if(parents.size()>1 && i<(parents.size()-1)) {
				s = s+",\n";
			}
		}
		
		// Add a close parenthesis if there are parent nodes
		if(child.is_input == false) {
			s = s+")";
		}
		
		return s;
	}

	/********************
	Convert the gate values of a node to string representation

	Node n: a node in the network

	Return: a string which represents the type of node 
	********************/
	private String gateToString(Node n) {
		String gate_type="";
		
		switch(n.gate_type) { 
			case X1:
				gate_type = "X1"; 
				break;
			case X2:
				gate_type = "X2"; 
				break;
			case X3:
				gate_type = "X3";
				break;
			case X4:
				gate_type = "X4"; 
				break;
			case X5:
				gate_type = "X5"; 
				break;
			case X1NOT:
				gate_type = "X1'";
				break;
			case X2NOT:
				gate_type = "X2'"; 
				break;
			case X3NOT:
				gate_type = "X3'"; 
				break;
			case X4NOT:
				gate_type = "X4'"; 
				break;
			case X5NOT:
				gate_type = "X5'";
				break;
			case LOW:
				gate_type = "0";
				break;
			case HIGH:
				gate_type = "1";
				break;
			case AND:
				gate_type = "AND"+all_nodes.indexOf(n);
				break;
			case OR:
				gate_type = "OR"+all_nodes.indexOf(n);
				break;
			case NAND:
				gate_type = "NAND"+all_nodes.indexOf(n);
				break;
			case NOR:
				gate_type = "NOR"+all_nodes.indexOf(n);
				break;
			case XOR:
				gate_type = "XOR"+all_nodes.indexOf(n);
				break;
			case XNOR:
				gate_type = "XNOR"+all_nodes.indexOf(n);
				break;
		}	
		
		return gate_type;
	}

	/********************
	Converts a string to it's corresponding gate representation.

	String gate: a string string which represents a gate

	Return: an int value which corresponds to the gate String 
	********************/
	private int stringToGate(String gate) {
		int gateType=-1;

		// Remove any integers from the end of the gate types
		String gateMod = removeNum(gate);

		switch(gateMod) {
			case "X1":
				gateType=X1;
			break;
			case "X2":
				gateType=X2;
			break;
			case "X3":
				gateType=X3;
			break;
			case "X4":
				gateType=X4;
			break;
			case "X5":
				gateType=X5;
			break;
			case "X1'":
				gateType=X1NOT;
			break;
			case "X2'":
				gateType=X2NOT;
			break;
			case "X3'":
				gateType=X3NOT;
			break;
			case "X4'":
				gateType=X4NOT;
			break;
			case "X5'":
				gateType=X5NOT;
			break;
			case "0":
				gateType=LOW;
			break;
			case "1":
				gateType=HIGH;
			break;
			case "AND":
				gateType=AND;
			break;
			case "OR":
				gateType=OR;
			break;
			case "NAND":
				gateType=NAND;
			break;
			case "NOR":
				gateType=NOR;
			break;
			case "XOR":
				gateType=XOR;
			break;
			case "XNOR":
				gateType=XNOR;
			break;
		}

		return gateType;
	}

	/********************
	Remove the number from the end of a gate string

	String gate: A string representation of a gate, from the database file

	Return: the gate String without the index value at the end
	********************/
	private String removeNum(String gate) {
		if(gate.charAt(0) == '0' || gate.charAt(0) == '1') {
			return gate;
		} else if (gate.charAt(0) == 'X' && gate.length() > 3) {
			String sub = gate.replaceAll("[^A-Z]","");
			return sub;
		} else if (gate.charAt(0) == 'X' && gate.length() <= 3) {
			return gate;
		} else {
			String sub = gate.replaceAll("[^A-Z]","");
			return sub;
		}
	}

	/********************
	Return to the user the truth table of the current network

	Return: An integer array which represents the truth table
	********************/
	public int[] getTruthTable() {
		return truth_table;
	}

}