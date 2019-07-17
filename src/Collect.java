import java.util.*;
import java.lang.Object;
import java.lang.Math;

public class Collect {

	Model model;
	public Collect(Model model) {
		this.model = model;
	}

	public void collect_data() {
		ArrayList<Scientist> sci_list = model.scientist_list;
		ArrayList<Idea> id_list = model.idea_list;
		double sci_plus_one = sci_list.size() + 1;
		double idea_plus_one = id_list.size() + 1;
		double tp_plus_one = model.tp_alive + 1;

		// Creates data structure containing scientist returns by idea
		ArrayList<ArrayList<Double>> sci_returns_total = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> ideas = new ArrayList<Double>();
		// Make row containing the idea indices (first column blank)
		ideas.add(null);
		for(double n = 0.0; n < idea_plus_one; n++) {
			ideas.add(n);
		}
		sci_returns_total.add(ideas);
		// Add each scientist's returns array, making the first index in the list the scientist ID
		for(int n = 0; n < sci_plus_one; n++) {
			Scientist sci = sci_list.get(n);
			double idx = (double) n;
			sci.returns_tot.add(0, idx);
			sci_returns_total.add(sci.returns_tot);
		}

		ArrayList<ArrayList<Double>> sci_returns_tot_cum = new ArrayList<ArrayList<Double>>();
		for(int n = 0; n < sci_plus_one; n++) {
			Scientist sci = sci_list.get(n);
			double idx = (double) n;
			ArrayList<Double> scientist_returns = new ArrayList<Double>();
			scientist_returns.add(idx);
			double overall_returns = sci.overall_returns_tp.stream().mapToDouble(i -> i).sum();
			scientist_returns.add(overall_returns);
			sci_returns_tot_cum.add(scientist_returns);
		}

		ArrayList<ArrayList<Double>> sci_overall_returns_tp = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> num_tp_dbl = new ArrayList<Double>();
		num_tp_dbl.add(null);			
		for(double n = 0.0; n < tp_plus_one; n++) {
			num_tp_dbl.add(n);
		}
		sci_overall_returns_tp.add(num_tp_dbl);
		for(int n = 0; n < sci_plus_one; n++) {
			Scientist sci = sci_list.get(n);
			double idx = (double) n;
			sci.overall_returns_tp.add(0, idx);
			sci_overall_returns_tp.add(sci.overall_returns_tp);
		}
		
		ArrayList<ArrayList<Integer>> idea_effort_by_tp = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> num_tp_int = new ArrayList<Integer>();
		num_tp_int.add(null);			
		for(int n = 0; n < tp_plus_one; n++) {
			num_tp_int.add(n);
		}
		idea_effort_by_tp.add(num_tp_int);
		for(int n = 0; n < idea_plus_one; n++) {
			Idea idea = id_list.get(n);
			idea.effort_by_tp.add(0, n);
			idea_effort_by_tp.add(idea.effort_by_tp);
		}

		ArrayList<ArrayList<Integer>> idea_total_effort = new ArrayList<ArrayList<Integer>>();
		for(int n = 0; n < idea_plus_one; n++) {
			Idea idea = id_list.get(n);
			ArrayList<Integer> idea_effort = new ArrayList<Integer>();
			idea_effort.add(n);
			idea_effort.add(idea.total_effort);
			idea_total_effort.add(idea_effort);
		}
	}
}