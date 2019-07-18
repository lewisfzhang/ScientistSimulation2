import java.util.ArrayList;

public class Smart_Optimize {
	// ArrayList of perceived ideas, discovered and k is paid
	// ArrayList of perceived ideas, discovered but k not paid
	// Feed Array list of d, not p ideas into the binary tree
	// Run Lagrange optimization algorithm on every node in the binary tree
	// Choose the allocation of effort that maximizes utility in current time period
	// Return the vector of effort across all ideas
	public ArrayList<Double> final_effort_alloc;
	
	public void smart_optimize(Scientist sci) {
		BTree b = new BTree(sci.ideas_k_paid_tot, sci.discov_ideas);

		double max = Integer.MIN_VALUE;
		ArrayList<Integer> max_branch = null;
		for (int i = 0; i<b.num_paths; i++) {
			ArrayList<Integer> branch = b.next_branch(); // next_branch() should not return null while running under this for loop
			double val = Economics.get_V(branch, sci);
			if (val > max) {
				max = val;
				max_branch = branch;
			}
		}

		final_effort_alloc = Economics.get_effort_alloc(max_branch);
	}
}


