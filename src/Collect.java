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
		for (int i = 0; i < num_k_total_idea_tp.length; i++) { // ideas
			Idea idea = model.idea_list.get(i);
			for (int j = 0; j < num_k_total_idea_tp[0].length; j++) { // tp
				if (j == 0) {
					num_k_total_idea_tp[i][j] = idea.num_k_by_tp.get(j);
					T_total_idea_tp[i][j] = idea.effort_by_tp.get(j);

				} else {
					num_k_total_idea_tp[i][j] = num_k_total_idea_tp[i][j - 1] + idea.num_k_by_tp.get(j); // cumulutative
					T_total_idea_tp[i][j] = T_total_idea_tp[i][j - 1] + idea.effort_by_tp.get(j);
				}
			}
		}
		Functions.int_arraylist_2d_to_csv(Functions.int_arr2d_to_list2d(num_k_total_idea_tp), "num_k_total_idea_tp", "tp", model);
		Functions.double_arraylist_2d_to_csv(Functions.double_arr2d_to_list2d(T_total_idea_tp), "T_total_idea_tp", "tp", model);

		// saving idea and its properties
		StringBuilder str = new StringBuilder("sci, idea, max, mean, sds").append(System.lineSeparator());
		for (Scientist sci : model.scientist_list) {
			for (int idea_idx = 0; idea_idx < model.idea_list.size(); idea_idx++) {
				double max = sci.perceived_rewards.get("Idea Max").get(idea_idx);
				double mean = sci.perceived_rewards.get("Idea Mean").get(idea_idx);
				double sds = sci.perceived_rewards.get("Idea SDS").get(idea_idx);
				str.append(sci.id).append(",")
						.append(idea_idx).append(",")
						.append(Functions.round_double(max)).append(",")
						.append(Functions.round_double(mean)).append(",")
						.append(Functions.round_double(sds)).append(System.lineSeparator());
			}
		}
		Functions.string_to_csv(str.toString(), model.config.parent_dir + "/data/model/perceived_ideas.csv", false);

		// finding the tp that each scientist was born
		int[] tp_born = new int[model.scientist_list.size()];
		int idx = 0;
		for (int tp = 0; tp<model.num_sci_tp.size(); tp++) {
			for (int j=0; j<model.num_sci_tp.get(tp); j++) {
				tp_born[idx] = tp;
				idx++;
			}
		}

		// saving all possible state spaces
		str = new StringBuilder("age, q, T, max, mean, sds, impact_left").append(System.lineSeparator()); // columns in the CSV file
		for (Scientist sci : model.scientist_list) {
			for (int age = 0; age<sci.tp_alive; age++) {
				// int age_left = sci.tp_alive - age;
				int tp = age + tp_born[sci.id];
				if (tp >= model.config.time_periods) tp = model.config.time_periods - 1; // keep array in bounds, doesn't matter if some data on older scientists is cut
				for (int idea_idx=0; idea_idx<model.idea_list.size(); idea_idx++) {
					Idea i = model.idea_list.get(idea_idx);
					int q = num_k_total_idea_tp[idea_idx][tp];
					double T = T_total_idea_tp[idea_idx][tp];
					double max = sci.perceived_rewards.get("Idea Max").get(idea_idx);
					double mean = sci.perceived_rewards.get("Idea Mean").get(idea_idx);
					double sds = sci.perceived_rewards.get("Idea SDS").get(idea_idx);
					double impact_left = i.idea_max * (1 - Idea.logistic_cdf(T, i.idea_mean, i.idea_sds));
					str.append(age).append(",")
							.append(q).append(",")
							.append(Functions.round_double(T)).append(",")
							.append(Functions.round_double(max)).append(",")
							.append(Functions.round_double(mean)).append(",")
							.append(Functions.round_double(sds)).append(",")
							.append(Functions.round_double(impact_left)).append(System.lineSeparator());
				}
			}
		}
		Functions.string_to_csv(str.toString(), model.config.parent_dir + "/data/nn/V0_data.csv", append);

		// saving all action spaces chosen by scientists in the model (not all possible action spaces)
		str = new StringBuilder("sci_id, tp, age, actual_returns");
		for (int i=0; i<Config.max_ideas; i++) str.append(",").append(i);
		str.append(System.lineSeparator());
		for (ArrayList<Integer> a : model.transactions) {
			// special unhandled cases
			if (a.size() <= 2 || a.size() - 2 >= Config.max_ideas) { // exceeds action space num_ideas, or no ideas in action space
				System.out.println("\nERROR: action space invalid...see collect.neural_net_data()");
				System.out.println(a + "\n");
				continue;
			}

			int sci_id = a.get(0); // index of scientist in model scientist list
			int tp = a.get(1);
			int sci_age = tp - tp_born[sci_id]; // use scientist age --> alpha, convert using sci_id
			double sci_tp_returns = model.scientist_list.get(sci_id).overall_returns_tp.get(sci_age); // overall_returns_tp idx based on scientist age, not model tp
			str.append(sci_id).append(",")
					.append(tp).append(",")
					.append(sci_age).append(",")
					.append(Functions.round_double(sci_tp_returns));
			for (int x=2; x<a.size(); x++) str.append(",").append(a.get(x));
			str.append(System.lineSeparator());
		}
		Functions.string_to_csv(str.toString(), model.config.parent_dir + "/data/nn/V1_data.csv", append);
	}

	void collect_data() {
		// FOR SCIENTISTS
		ArrayList<ArrayList<Double>> sci_overall_returns_tp = new ArrayList<>(); // Returns by time period, DataFrame
		for (Scientist sci : model.scientist_list) sci_overall_returns_tp.add(sci.overall_returns_tp);
		Functions.double_arraylist_2d_to_csv(sci_overall_returns_tp, "sci_overall_returns_tp", "tp", model);

		// FOR IDEAS
		ArrayList<ArrayList<Double>> idea_effort_by_tp = new ArrayList<>(); // Effort by time period, DataFrame
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