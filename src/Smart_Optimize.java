import java.util.ArrayList;

public class Smart_Optimize {
	// ArrayList of perceived ideas, discovered and k is paid
	// ArrayList of perceived ideas, discovered but k not paid
	// Feed Array list of d, not p ideas into the binary tree
	// Run Lagrange optimization algorithm on every node in the binary tree
	// Choose the allocation of effort that maximizes utility in current time period
	// Return the vector of effort across all ideas
	public ArrayList<Double> final_effort_allocation;
	
	public void smart_optimize(Scientist sci) {
		BTree b = new BTree(sci.ideas_k_paid_tot, sci.discov_ideas);
		
	}
}


