import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class Collect {
	Model model;

	Collect(Model model) {
		this.model = model;
	}

	// saves all necessary training data to CSV
	void neural_net_data(boolean append) {
		int[][] num_k_total_idea_tp = new int[model.idea_list.size()][model.config.time_periods]; // get Q --> exp number of scientists who have worked on each idea per tp
		double[][] T_total_idea_tp = new double[model.idea_list.size()][model.config.time_periods]; // T --> total effort in each idea
		for (int i=0; i<num_k_total_idea_tp.length; i++) { // ideas
			Idea idea = model.idea_list.get(i);
			for (int j=0; j<num_k_total_idea_tp[0].length; j++) { // tp
				if (j==0) {
					num_k_total_idea_tp[i][j] = idea.num_k_by_tp.get(j);
					T_total_idea_tp[i][j] = idea.effort_by_tp.get(j);

				} else {
					num_k_total_idea_tp[i][j] = num_k_total_idea_tp[i][j-1] + idea.num_k_by_tp.get(j); // cumulutative
					T_total_idea_tp[i][j] = T_total_idea_tp[i][j-1] + idea.effort_by_tp.get(j);
				}
			}
		}

		int[] tp_born = new int[model.scientist_list.size()];
		int idx = 0;
		for (int tp = 0; tp<model.num_sci_tp.size(); tp++) {
			for (int j=0; j<model.num_sci_tp.get(tp); j++) {
				tp_born[idx] = tp;
				idx++;
			}
		}

		StringBuilder str = new StringBuilder("age_left, q, T, max, mean, sds").append(System.lineSeparator()); // columns in the CSV file
		for (Scientist sci : model.scientist_list) {
			for (int age = 0; age<sci.tp_alive; age++) {
				int age_left = sci.tp_alive - age;
				int tp = age + tp_born[sci.id];
				if (tp >= model.config.time_periods) tp = model.config.time_periods - 1; // keep array in bounds, doesn't matter if some data on older scientists is cut
				for (int idea_idx=0; idea_idx<model.idea_list.size(); idea_idx++) {
					int q = num_k_total_idea_tp[idea_idx][tp];
					double T = T_total_idea_tp[idea_idx][tp];
					double max = sci.perceived_rewards.get("Idea Max").get(idea_idx);
					double mean = sci.perceived_rewards.get("Idea Mean").get(idea_idx);
					double sds = sci.perceived_rewards.get("Idea SDS").get(idea_idx);
					str.append(age_left).append(",")
							.append(q).append(",")
							.append(Functions.round_double(T)).append(",")
							.append(Functions.round_double(max)).append(",")
							.append(Functions.round_double(mean)).append(",")
							.append(Functions.round_double(sds)).append(System.lineSeparator());
				}
			}
		}
		BufferedWriter bw; // leave this section of duplicated code because we are "appending" to the file
		try {
			bw = new BufferedWriter(new FileWriter(model.config.parent_dir + "/data/nn_data.csv", append));
			bw.write(str.toString());
			bw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	void collect_data() {
		// FOR SCIENTISTS
		ArrayList<ArrayList<Double>> sci_overall_returns_tp = new ArrayList<>(); // Effort by time period, DataFrame
		for (Scientist sci : model.scientist_list) sci_overall_returns_tp.add(sci.overall_returns_tp);
		Functions.double_arraylist_2d_to_csv(sci_overall_returns_tp, "sci_overall_returns_tp", "tp", model);

		// FOR IDEAS
		ArrayList<ArrayList<Double>> idea_effort_by_tp = new ArrayList<>(); // Overall returns by time period, DataFrame
		ArrayList<ArrayList<Integer>> idea_num_k_by_tp = new ArrayList<>(); // Number of researchers by time period, DataFrame
		for (Idea i : model.idea_list) idea_effort_by_tp.add(i.effort_by_tp);
		for (Idea i : model.idea_list) idea_num_k_by_tp.add(i.num_k_by_tp);
		Functions.double_arraylist_2d_to_csv(idea_effort_by_tp, "idea_effort_by_tp", "tp", model);
		Functions.int_arraylist_2d_to_csv(idea_num_k_by_tp, "idea_num_k_by_tp", "tp", model);

		ArrayList<Double> idea_total_effort = new ArrayList<>();
		ArrayList<Double> idea_sci_impact = new ArrayList<>();
		ArrayList<Integer> idea_num_k = new ArrayList<>();
		ArrayList<Integer> idea_time_to_inflect = new ArrayList<>();
		for (Idea i : model.idea_list) idea_total_effort.add(i.total_effort);
		for (Idea i : model.idea_list) idea_sci_impact.add(Idea.get_returns(i.idea_mean, i.idea_sds, i.idea_max, 0, i.total_effort));
		for (Idea i : model.idea_list) idea_num_k.add(i.num_k_total);
		for (Idea i : model.idea_list) {
			double curr_sum = 0;
			int idx = -1;
			int first = -1;
			for (int x=0; x<i.effort_by_tp.size(); x++) {
				double val = i.effort_by_tp.get(x);
				if (first == -1 && val > 0) first = x;
				curr_sum += val;
				if (curr_sum >= i.idea_mean) {
					idx = x;
					break;
				}
			}
			idea_time_to_inflect.add((first == idx) ? -1 : idx); // returns -1 if either 1) idea was never invested, 2) idea invested, but never reached inflection point
		}
		HashMap<String, String> data = new HashMap<>(); // key = array name, value = array data
		data.put("idea_total_effort", Functions.double_arraylist_to_csv_string(idea_total_effort));
		data.put("idea_sci_impact", Functions.double_arraylist_to_csv_string(idea_sci_impact));
		data.put("idea_num_k", Functions.int_arraylist_to_csv_string(idea_num_k));
		data.put("idea_time_to_inflect", Functions.int_arraylist_to_csv_string(idea_time_to_inflect));
		Functions.hashmap_to_csv(data, idea_total_effort.size(),"idea_arrays", "idea", model);
	}
}