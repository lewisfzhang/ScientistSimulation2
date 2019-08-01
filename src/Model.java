import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Model implements java.io.Serializable {
	// initiates the key parameters within the model, as set in config
	Config config;

	// SCALARS, all updated in model step
	int num_sci;

	int ideas_per_time;
	int tp = 0;

	// ARRAYS: creates empty arraylists to track scientists and ideas; index indicates age
	ArrayList<Scientist> scientist_list = new ArrayList<>();
	ArrayList<Idea> idea_list = new ArrayList<>();
	ArrayList<Integer> num_sci_tp = new ArrayList<>();

	// model constructor
	Model(Config config) {
		this.config = config;
	}

	// defines the process for one time period within the model
	void step() {
		// recompute random number of ideas and scientists born within this time period
		num_sci = Functions.poisson(config.sci_rate, config);
		ideas_per_time = Functions.poisson(config.ideas_per_time, config);

		age_scientists();
		birth_new_scientists();

		int ideas_last_tp = birth_new_ideas(); // keep track of how many old ideas so we only have to update new ideas
		update_scientist_idea(ideas_last_tp);

		if (config.funding) {
			distribute_funding();
		}

		for (Scientist sci : scientist_list) {
			sci.step();
		}

		update_objects();
		pay_out_returns();

		tp++;
	}

	// adds one year to the age of every scientist that already exists within the model
	void age_scientists() {
		for (Scientist sci : scientist_list) {
			sci.age++;
		}
	}

	// creates new scientists, birthed at age 0, and sets their random constants (variances, learning speed, and effort)
	void birth_new_scientists() {
		for(int s = 0; s < num_sci; s++) {
			Scientist sci = new Scientist(this);
			scientist_list.add(sci);
		}
		num_sci_tp.add(num_sci);
	}

	// creates new ideas and sets their random constants (true mean, true max, investment cost)
	// returns the number of ideas from last tp
	int birth_new_ideas() {
		for(int i = 0; i < ideas_per_time; i++) {
			Idea idea = new Idea(this);
			idea_list.add(idea);
		}
		// return should be 0 if tp = 0 because there are no previous ideas before tp = 0 (see set_perceived_rewards)
		return (tp == 0) ? 0 : idea_list.size() - ideas_per_time;
	}

	// loop through every scientist, appending their perceived rewards dataframe with new ideas
	// also updates related list with extra spots for new ideas --> append_scientist_lists
	void update_scientist_idea(int ideas_last_tp) {
		for (Scientist sci : scientist_list) {
			// determining how many loops we need to run (for performance efficiency)
			// just born scientists need to update for all ideas --> sci.age = 0
			// older scientists only need to update for new ideas
			int idea_list_start_idx = (sci.age == 0) ? 0 : ideas_last_tp;

			// slice to iterate only through new ideas, setting up attributes and scientist perception
			for (int i = idea_list_start_idx; i < idea_list.size(); i++) {
				Idea idea = idea_list.get(i);
				append_scientist_lists(sci); // add element to signal new idea for data collector variables

				// keeping all normal distributions for sci_mult to 0.3 range --> *** 0.1 sds ***
				// idea is that sci.idea_mult ranges from 0.5-1.5, so the worst case we get low end of 0.2
				double sci_mult_max = Functions.get_normal_number(sci.idea_max_mult, 0.1, config);
				double sci_mult_mean = Functions.get_normal_number(sci.idea_sds_mult, 0.1, config);
				double sci_mult_sds = Functions.get_normal_number(sci.idea_mean_mult, 0.1, config);

				double idea_mean = sci_mult_mean * idea.idea_mean;
				double idea_sds = sci_mult_sds * idea.idea_sds;
				double idea_max = sci_mult_max * idea.idea_max;
				double idea_k = Math.round(sci.learning_speed * idea.idea_k); // rounds to nearest integer because technically k should also be an int --> units of effort

				// updating back to perceived_rewards hashmap
				sci.perceived_rewards.get("Idea Mean").add(idea_mean);
				sci.perceived_rewards.get("Idea SDS").add(idea_sds);
				sci.perceived_rewards.get("Idea Max").add(idea_max);
				sci.perceived_rewards.get("Idea K").add(idea_k);
			}

			// check the previous period to see which newly discovered ideas had their entry costs paid --> removes/sets to 0 the ones that weren't learned, aka forgotten
			for (int i = 0; i < sci.ideas_k_paid_tot.size(); i++) {
				if (sci.discov_ideas.get(i) == 1 & sci.ideas_k_paid_tot.get(i) == 0)
					sci.discov_ideas.set(i, 0);
			}

			// determine which ideas a scientist will discover
			int count = 0;
			int tru_rate = (sci.age == 0) ? (int) (2 * sci.discov_rate) : (int) sci.discov_rate; // scientist just born discovers twice as many ideas
			while (count < tru_rate) {
				int new_idea_idx = Functions.get_random_int(0, idea_list.size(), config);
				if (sci.discov_ideas.get(new_idea_idx) == 0) { // only update if idea hasn't been discovered
					sci.discov_ideas.set(new_idea_idx, 1);
					count++;
				}
			}
		}
	}

	// updates the lists within each scientist object to reflect the correct number of available ideas
	// ignore static warning, only because we aren't using self keyword
	// keep it in model since it is called by the model step function --> set_perceived_rewards()
	void append_scientist_lists(Scientist sci) {
		sci.idea_eff_tp.add(0.0);
		sci.idea_eff_tot.add(0.0);
		sci.ideas_k_paid_tp.add(0);
		sci.ideas_k_paid_tot.add(0);
		sci.discov_ideas.add(0);
		sci.returns_tp.add(0.0);
		sci.returns_tot.add(0.0);
	}

	// pays out grant money from the budget in each period before scientists make investments
	void distribute_funding() {
		ArrayList<Scientist> old_sci = new ArrayList<>();
		ArrayList<Scientist> young_sci = new ArrayList<>();
		int scientists_alive = 0;
		for(Scientist sci : scientist_list) {
			if(sci.age < (int) (0.5 * (double) sci.tp_alive)) {
				young_sci.add(sci);
				scientists_alive++;
			}
			else if(sci.age < sci.tp_alive) {
				old_sci.add(sci);
				scientists_alive++;
			}
		}
		int total_budget = (int) (config.budget_prop * scientists_alive * config.start_effort_mean);
		int total_recipients = (int) (scientists_alive * config.budget_prop);
		int grant_size = total_budget / total_recipients;
		
		HashMap<Integer, Double> grant_buckets = new HashMap<>();
		grant_buckets.put(1, config.y_k_n);
		grant_buckets.put(2, config.y_k_b);
		grant_buckets.put(3, config.y_e_n);
		grant_buckets.put(4, config.y_e_b);
		grant_buckets.put(5, config.o_k_n);
		grant_buckets.put(6, config.o_k_b);
		grant_buckets.put(7, config.o_e_n);
		grant_buckets.put(8, config.o_e_b);
		for(int i = 1; i <= 8; i++) {
			int grant_budget = (int) (grant_buckets.get(i) * total_recipients);
			while(grant_budget > 0) {
				assign_individual_grants(i, grant_size, young_sci, old_sci);
				grant_budget--;
			}
		}
	}

	void assign_individual_grants(int i, int grant_size, ArrayList<Scientist> young_sci, ArrayList<Scientist> old_sci) {
		boolean young_sci_rec = (i <= 4);
		boolean k_grant_rec = (i == 1 || i == 2 || i == 5 || i == 6);
		boolean new_idea_rec = (i % 2 != 0);
		if(young_sci_rec) {
			int recipient = Functions.get_random_int(0, young_sci.size(), config);
			Scientist sci = young_sci.get(recipient);
			
		}
	}

	// data collection: loop through each idea object, updating the effort that was invested in this time period
	void update_objects() {
		for(int idx = 0; idx < idea_list.size(); idx++) {
			Idea idea = idea_list.get(idx);
			double effort_invested_tp = 0;
			int k_paid_tp = 0; // number of scientists who learned the idea in this tp

			for (Scientist sci : scientist_list) {
				effort_invested_tp += sci.idea_eff_tp.get(idx);
				k_paid_tp += sci.ideas_k_paid_tp.get(idx);
			}

			idea.total_effort += effort_invested_tp;
			idea.num_k_total += k_paid_tp;
			idea.effort_by_tp.add(effort_invested_tp);
			idea.num_k_by_tp.add(k_paid_tp);
		}
	}

	// determine who gets paid out based on the amount of effort input
	void pay_out_returns() {
		for(int i = 0; i < idea_list.size(); i++) {
			Idea idea = idea_list.get(i);
			if(idea.effort_by_tp.get(tp) > 0) {
				double start_effort = idea.total_effort - idea.effort_by_tp.get(tp); // since we already updated effort_by_tp in update_objects()
				double end_effort = idea.total_effort;
				double idea_returns = Idea.get_returns(idea.idea_mean, idea.idea_sds, idea.idea_max, start_effort, end_effort);
				process_winners(i, idea_returns); // process the winner for each idea, one per loop
			}
		}

		for (Scientist sci : scientist_list) {
			double tp_returns = sci.returns_tp.stream().mapToDouble(i -> i).sum(); // sum of sci.returns_tp
			sci.overall_returns_tp.add(tp_returns);
		}
	}

	// processes winners for idea with index iidx
	void process_winners(int iidx, double idea_returns) {
		ArrayList<Integer> list_of_investors = new ArrayList<>();
		for(int s = 0; s < scientist_list.size(); s++) {
			Scientist sci = scientist_list.get(s);
			if(sci.idea_eff_tp.get(iidx) != 0) {
				list_of_investors.add(s);
			}
		}

		if(config.equal_returns) { // each scientist receives returns proportional to effort
			double total_effort_invested = 0; // double so we can do "double" division in 2nd for loop
			for (int sci_id : list_of_investors) {
				Scientist sci = scientist_list.get(sci_id);
				total_effort_invested += sci.idea_eff_tp.get(iidx);
			}
			for (int sci_id : list_of_investors) { // must be separate for loop from above since we need to calculate total_effort_invested first
				Scientist sci = scientist_list.get(sci_id);
				double individual_proportion = sci.idea_eff_tp.get(iidx) / total_effort_invested;
				double individual_returns = individual_proportion * total_effort_invested;
				Functions.arr_increment_double(sci.returns_tp, iidx, individual_returns);
				Functions.arr_increment_double(sci.returns_tot, iidx, individual_returns);
			}
		} else {
			int oldest_scientist_id = list_of_investors.get(0); // scientist born "earliest" in same tp should come first in list
			Scientist sci = scientist_list.get(oldest_scientist_id);
			Functions.arr_increment_double(sci.returns_tp, iidx, idea_returns);
			Functions.arr_increment_double(sci.returns_tot, iidx, idea_returns);
		}
	}
}
