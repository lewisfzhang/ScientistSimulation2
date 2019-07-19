import java.util.ArrayList;
import java.util.HashMap;

public class Smart_Optimize {
	// ArrayList of perceived ideas, discovered and k is paid
	// ArrayList of perceived ideas, discovered but k not paid
	// Feed Array list of d, not p ideas into the binary tree
	// Run Lagrange optimization algorithm on every node in the binary tree
	// Choose the allocation of effort that maximizes utility in current time period
	// Return the vector of effort across all ideas
	ArrayList<Double> final_effort_alloc;
	
	HashMap<String, ArrayList<Integer>> smart_optimize(Scientist sci) {
		BTree b = new BTree(sci.ideas_k_paid_tot, sci.discov_ideas);

		double max = Integer.MIN_VALUE;
		ArrayList<Integer> max_branch = null;
		for (int i = 0; i<b.num_paths; i++) {
			ArrayList<Integer> branch = b.next_branch(); // next_branch() should not return null while running under this for loop
			double val = get_V(branch, sci);
			if (val > max) {
				max = val;
				max_branch = branch;
				final_effort_alloc = temp_effort_alloc;
			}
		}

		return process_eff(sci);

		// return inv_dict; should return inv_dict!
	}

	ArrayList<Double> temp_effort_alloc; // used to keep track, since java can only return one Object

	double get_V(ArrayList<Integer> branch, Scientist sci) {
		// packages data
		// stores data as text file
		// calls Python file that reads in text file --> maybe make Python file a binary executable
		// Python file outputs to text file (if you use Java cal
		return 0;
	}

	HashMap<String, ArrayList<Integer>> process_eff(Scientist sci) {
		HashMap<String, ArrayList<Integer>> inv_dict = new HashMap<>();
		inv_dict.put("idea_idx", new ArrayList<>());
		inv_dict.put("marg_eff", new ArrayList<>());
		inv_dict.put("k_paid", new ArrayList<>());

		return inv_dict;
	}
}


