import java.util.ArrayList;
import java.util.HashMap;

public class Smart_Optimize {
	// Feed Array list of d, not p ideas into the binary tree
	// Run Lagrange optimization algorithm on every node in the binary tree
	// Choose the allocation of effort that maximizes utility in current time period
	// Return the vector of effort across all ideas

	HashMap<String, ArrayList<Double>> smart_optimize(Scientist sci) {
		BTree b = new BTree(sci.ideas_k_paid_tot, sci.discov_ideas);

		double max = Integer.MIN_VALUE;
		ArrayList<Double> final_effort_alloc = new ArrayList<>(); // should have all ideas, same structure as model.idea_list
		for (int i = 0; i<b.num_paths; i++) {
			ArrayList<Integer> branch = b.next_branch(); // next_branch() should not return null while running under this for loop
			double val = get_V(branch, sci); // should only have 4 branches --> 2 new ideas per period
			if (val > max) {
				max = val;
				final_effort_alloc = temp_effort_alloc;
			}
		}

		return process_eff(sci, final_effort_alloc);
	}

	ArrayList<Double> temp_effort_alloc; // used to keep track, since java can only return one Object

	double get_V(ArrayList<Integer> branch, Scientist sci) {
		// packages data
		// stores data as text file
		// calls Python file that reads in text file --> maybe make Python file a binary executable
		// Python file outputs to text file (if you use Java cal
		return 0;
	}

	HashMap<String, ArrayList<Double>> process_eff(Scientist sci, ArrayList<Double> final_effort_alloc) {
		HashMap<String, ArrayList<Double>> inv_dict = new HashMap<>();
		inv_dict.put("idea_idx", new ArrayList<>());
		inv_dict.put("marg_eff", new ArrayList<>());
		inv_dict.put("k_paid", new ArrayList<>());

		for (int i = 0; i<final_effort_alloc.size(); i++) {
			double eff = final_effort_alloc.get(i);
			if (eff > 0) {
				inv_dict.get("idea_idx").add((double) i);
				inv_dict.get("marg_eff").add(eff);
				inv_dict.get("k_paid").add((sci.ideas_k_paid_tot.get(i) == 0) ? 1.0 : 0.0);
			}
		}
		return inv_dict;
	}
}


