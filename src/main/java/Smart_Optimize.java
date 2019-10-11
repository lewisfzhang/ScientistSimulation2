import java.util.*;
import java.util.stream.IntStream;

class Smart_Optimize {
	HashMap<Integer, Double> final_effort_alloc; // should have all ideas, same structure as model.idea_list
	HashMap<Integer, Double> temp_effort_alloc; // used to keep track, since java can only return one Object

	ArrayList<Integer> line; // keeps track of discovered ideas where k was already paid by last tp
	boolean is_line = false;

	double[] V0_val_list; // initialize all values to 0

	// compute V0's for all discovered ideas in this tp --> can compute now because state space states constant within a tp --> speed up performance hopefully
	void compute_V0(Scientist sci) {
		V0_val_list = new double[sci.model.idea_list.size()]; // initialize all values to 0
		if (sci.model.config.use_neural) {
			for (int idx=0; idx<sci.discov_ideas.size(); idx++) { // discov ideas should be same size as idea_list because it tracks idx of idea in idea_list
				if (sci.discov_ideas.get(idx) == 1) { // discovered
					Idea i = sci.model.idea_list.get(idx);
					double q = i.num_k_total;
					double T = i.total_effort; // total effort T
					double max = sci.perceived_rewards.get("Idea Max").get(idx); // M
					double mean = sci.perceived_rewards.get("Idea Mean").get(idx); // lambda
					double sds = sci.perceived_rewards.get("Idea SDS").get(idx); // sigma
					double[] in_vec = {q, T, max, mean, sds}; // q, T, max, mean, sds
					V0_val_list[idx] = sci.model.neural.predict(0, sci.age, in_vec);
				}
			}
		}
	}

	// Feed Array list of d, not p ideas into the binary tree
	// Run Lagrange optimization algorithm on every node in the binary tree
	// Choose the allocation of effort that maximizes utility in current time period
	// Return the vector of effort across all ideas
	HashMap<String, ArrayList<Double>> investing_helper(Scientist sci) {
		// binary tree to generate all possible permutations of new ideas a scientist can learn in this TP
		BTree b = new BTree(sci.ideas_k_paid_tot, sci.discov_ideas);
		line = b.line;
		double max = Integer.MIN_VALUE;

		if (sci.model.config.use_neural) compute_V0(sci); // store state space V predictions

		if (!line.isEmpty()) { // if line isn't empty --> ideas have already been learned
			is_line = true;
			max = get_V(line, sci); // the case where scientist invests all k chunks of effort into ideas already learned
			final_effort_alloc = temp_effort_alloc;
			is_line = false;
		}

		for (int i = 1; i<=b.num_paths; i++) {
			ArrayList<Integer> branch = b.next_branch(i==b.num_paths); // gets next_branch(), isLast tells function whether it should generate next available branch
			if (branch.isEmpty()) {continue;} // skip initial case where scientist has not learn any ideas and doesn't learn any new ideas --> when sci.age == 0

			double val = get_V(branch, sci);
			if (val > max) {
				max = val;
				final_effort_alloc = temp_effort_alloc;
			}
		}

		/** HashMap<String, ArrayList<Double>> a = process_eff(sci);
		System.out.println(a);
		return a; */
		return process_eff(sci);
	}

	// packages data
	// stores data as text file
	// calls Python file that reads in text file --> maybe make Python file a binary executable
	// Python file outputs to text file
	double get_V(ArrayList<Integer> branch, Scientist sci) { // k = how many chunks of effort
		double avail_eff = sci.avail_effort;
		for (int idea_idx : branch) {
			if (sci.ideas_k_paid_tot.get(idea_idx) == 0) { // idea hasn't been learned, so we deduct from avail_eff
				avail_eff -= sci.perceived_rewards.get("Idea K").get(idea_idx);
			}
		}

		int k = 10; // k refers to how many chunks of effort (from 1 unit to 10 units)
		double increment = avail_eff / k;

		// based on Knuth's lexicographic permuation generation: http://lh3.ggpht.com/_bLHHR6rd5Ug/Sxn2isijPNI/AAAAAAAAAK8/1oSWhhjB7AI/s1600-h/AlgorithmL20.gif
		// idea from https://stackoverflow.com/questions/11570132/generator-functions-equivalent-in-java
		// generates all possible allocations/permutations of effort chunks across ideas that will be/have been learned
		Generator<HashMap<Integer, Double>> effort_perm_gen = new Generator<HashMap<Integer, Double>>() {
			public void run() throws InterruptedException {
				// n multichoose k multisets from the list of objects.  n is the size of the objects
				// NOTE: k is actually n --> objects to be placed in bins, n is actually k --> the bins
				// in other words, k = stars, objects = bars (array whose elements represent distinct bins) --> use this explanation
				int[] objects = IntStream.range(0, branch.size()).toArray(); // array from [0, end), follows indices of arraylist branch

				// multichoose function borrowed from https://github.com/ekg/multichoose/blob/master/multichoose.py, converted from python to java
				int j; int j_1; int q; // init here for scoping
				int r = objects.length - 1;
				int[] a = new int[k]; // initial multiset indexes, automatically fills array of 0's with length k
				while (true) {
					// out keeps track of index of the idea in arraylist branch
					int[] out = IntStream.range(0, k).map(i -> objects[a[i]]).toArray(); // in python words, [objects[a[i]] for i in range(0,k)] --> stores index of element in arraylist branch
					if (is_line) { // if we are just computing the line, don't need to check for new ideas
						yield(Opt_Func.eff_chunker(out, branch, increment));  // emit result, convert double array to double arraylist
					} else { // CAN IMPROVE HERE, MUST INCLUDE ALL IDEAS --> K IS CALCULATED BAWED ON THAT
						if (out[out.length-1] >= line.size()) { // if at least one new idea has effort invested in, line.size() = idx of first "new" idea in branch arraylist
							yield(Opt_Func.eff_chunker(out, branch, increment));  // emit result, convert double array to double arraylist
						} // else do nothing, go through another loop to find next permutation
					}

					j = k - 1;
					while (j >= 0 && a[j] == r) {j -= 1;}
					if (j < 0) {break;} // check for end condition
					j_1 = j;
					while (j_1 <= k - 1) {
						a[j_1] = a[j_1] + 1; // increment
						q = j_1;
						while (q < k - 1) {
							a[q + 1] = a[q]; // shift left
							q += 1;
						}
						q += 1;
						j_1 = q;
					}
				}
			}
		};

		double max_sum = Integer.MIN_VALUE;
		for (HashMap<Integer, Double> temp_temp_effort_alloc : effort_perm_gen) { // temp_temp_effort_alloc stores idea_idx as key, eff in each idea as value
			if (temp_temp_effort_alloc.size() > sci.model.config.max_ideas) continue; // don't include generations where effort is allocated across more than max_ideas number of ideas

			double U_e_sum = 0;
			ArrayList<Double> all_V0 = new ArrayList<>(); // stores V0's based on each idea to be used --> then updated to array in
			for (Map.Entry<Integer, Double> entry : temp_temp_effort_alloc.entrySet()) {
				int idx = entry.getKey(); // idx is index of idea
				double eff = entry.getValue();
				if (eff > 0) { // only ones with positive effort matter
					Idea i = sci.model.idea_list.get(idx);
					double mean = sci.perceived_rewards.get("Idea Mean").get(idx); // lambda
					double sds = sci.perceived_rewards.get("Idea SDS").get(idx); // sigma
					double max = sci.perceived_rewards.get("Idea Max").get(idx); // M
					double q = i.num_k_total;
					double start_idx = i.total_effort; // total effort T
					double end_idx = start_idx + eff * q; // T + e * q --> if learned already
					if (sci.ideas_k_paid_tot.get(idx) == 0) {end_idx += eff;} // q + 1 if haven't learned

					U_e_sum += Idea.get_returns(mean, sds, max, start_idx, end_idx) / q; // get sum of returns across ideas (U_e only, not V_a)
					if (sci.model.config.use_neural) all_V0.add(V0_val_list[idx]);

				}
			}

			double V_a_future = 0;
			if (sci.model.config.use_neural) {
				double[] in = new double[sci.model.config.max_ideas]; // input vector for V1, values initialized to 0 by default
				for (int i=0; i<all_V0.size(); i++) in[i] = all_V0.get(i); // transfer all elements of all_V0 to array in, the input vector
				V_a_future = sci.model.neural.predict(1, sci.age, in);
			}
			double V_a_present = U_e_sum + sci.model.config.BETA * V_a_future;

			if (V_a_present > max_sum) {
				max_sum = V_a_present;
				temp_effort_alloc = temp_temp_effort_alloc;
			}
		}
		return max_sum;
	}


	HashMap<String, ArrayList<Double>> process_eff(Scientist sci) {
		HashMap<String, ArrayList<Double>> inv_dict = new HashMap<>();
		inv_dict.put("idea_idx", new ArrayList<>());
		inv_dict.put("marg_eff", new ArrayList<>());
		inv_dict.put("k_paid", new ArrayList<>());

		for (Map.Entry<Integer, Double> entry : final_effort_alloc.entrySet()) {
			int idea_idx = entry.getKey();
			double eff = entry.getValue();

			// NOTE: IF STATEMENT SHOULD ALWAYS BE TRUE BECAUSE EACH ELEMENT IN FINAL_EFFORT_ALLOC IS A UNIQUE IDEA
			// keeping this format for consistency with Optimize class's update_df()
			int idx = inv_dict.get("idea_idx").indexOf((double) idea_idx);
			if (idx == -1) { // doesn't contain idea_idx yet, add new entry
				inv_dict.get("idea_idx").add((double) idea_idx);
				inv_dict.get("marg_eff").add(eff);
				inv_dict.get("k_paid").add((sci.ideas_k_paid_tot.get(idea_idx) == 0) ? 1.0 : 0.0);
			} else { // update existing entry for marginal effort (k_paid doesn't need to be updated cuz it's a constant)
				Functions.arr_increment_double(inv_dict.get("marg_eff"), idx, eff); // += eff
			}
		}

		return inv_dict;
	}
}
