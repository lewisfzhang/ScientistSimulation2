import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Model implements java.io.Serializable {
	// initiates the key parameters within the model, as set in config
	Config config;
	Neural neural;

	// SCALARS, all updated in model step
	int num_sci;

	int ideas_per_time;
	int tp = 0;

	// ARRAYS: creates empty arraylists to track scientists and ideas; index indicates age
	ArrayList<Scientist> scientist_list = new ArrayList<>();
	ArrayList<Idea> idea_list = new ArrayList<>();
	ArrayList<Integer> num_sci_tp = new ArrayList<>();
	ArrayList<ArrayList<Integer>> transactions = new ArrayList<>(); // sci_idx, tp, all idea_idx...

	// model constructor
	Model(Config config) {
		this.config = config;
		if (config.smart_opt && config.use_neural) neural = new Neural(config); // only add neural net if we are not training
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

		if (config.funding) distribute_funding(); // distribute grants if funding is turned on in config

		for (Scientist sci : scientist_list) {
			sci.step(); // initiate the scientist step function including optimization (in scientist class)
		}

		// update based on scientist optimization patters
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
		for(Scientist sci : scientist_list) {
			if(sci.age < (int) (0.5 * (double) sci.tp_alive)) young_sci.add(sci); // define young scientists as less than halfway through their "lifespan"
			else if(sci.age < sci.tp_alive) old_sci.add(sci); // define old scientists as those more than halfway through their lives
		}
		int scientists_alive = young_sci.size() + old_sci.size();

		double total_budget = config.budget_prop * scientists_alive * config.start_effort_mean; // total budget set as proportion of total effort in population
		double e_grant_size = config.e_grant_size_prop * total_budget; // divvies up the e grants into appropriate sizes

		for(int i = 1; i <= 6; i++) { // i refers to bucket number; loop through each bucket to distribute grants
			double grant_budget = config.grant_buckets.get(i) * total_budget; // set total budget for specific grant type
			ArrayList<Scientist> young_sci_eligible = new ArrayList<>(young_sci);
			ArrayList<Scientist> old_sci_eligible = new ArrayList<> (old_sci);
			// distribute grants while the budget is greater than 0
			while(grant_budget > 0) {
				if ((i <= 3 && young_sci_eligible.size() > 0) || (i > 3 && old_sci_eligible.size() > 0)) {
					double grant_size = assign_individual_grants(i, e_grant_size, grant_budget, young_sci_eligible, old_sci_eligible);
					if(grant_budget - Math.abs(grant_size) >= 0) grant_budget -= Math.abs(grant_size); // subtract grant size from budget if funding still available
					else break; // if the budget is less than grant size, don't distribute grant --> out of money
				}
				else break; // no scientist of grant type available to give funding to
			}
		}
	}

	double assign_individual_grants(int i, double e_grant_size, double grant_budget, ArrayList<Scientist> young_sci, ArrayList<Scientist> old_sci) {
		boolean young_sci_rec = (i <= 3); // true if grant is for young scientists
		boolean k_grant_rec = (i == 1 || i == 2 || i == 4 || i == 5); // true if k grant
		boolean new_idea_rec = ((i + 2) % 3 == 0); // true if grant is for a new idea
		double grant_size = 0;

		Scientist sci;
		int sci_ideas = 0;
		if(young_sci_rec) { // selects a random young scientist
			int recipient = Functions.get_random_int(0, young_sci.size(), config);
			sci = young_sci.get(recipient);

		} else { // selects a random old scientist
			int recipient = Functions.get_random_int(0, old_sci.size(), config);
			sci = old_sci.get(recipient);
		} // else return grant size of 0 --> already covered by above while loop in distribute_funding()

		ArrayList <Integer> fundable_ideas = new ArrayList<>();
		for(int j = 0; j < sci.discov_ideas.size(); j++) {
			boolean discovered = (sci.discov_ideas.get(j) == 1);
			if (discovered) fundable_ideas.add(j); // arraylist of discovered idea indices
		}

		//
		boolean idea_found = false;
		int idea_index = -2;
		if (!k_grant_rec) {
			idea_index = -1;
			grant_size = e_grant_size;
			idea_found = true;
		} else {
			while(!idea_found && fundable_ideas.size() > 0) {
				int idea_choice = Functions.get_random_int(0, fundable_ideas.size(), config);
				idea_index = fundable_ideas.get(idea_choice); // gets the original idea index of the selected idea
				Idea idea = idea_list.get(idea_index);
				int idea_phase = idea.phase();
				if ((new_idea_rec && idea_phase == 0) || (!new_idea_rec && idea_phase == 1)) { // new idea grant and new idea, or old idea grant and old idea
					grant_size = idea.idea_k * sci.learning_speed; // true: negative learning cost if k grant, false: standard e grant size if e grant
					idea_found = true;
				} else {
					fundable_ideas.remove(idea_choice);
				}
			}
		}
		if(idea_found && (grant_budget - Math.abs(grant_size)) >= 0) { // assumption is idea_idx > -2 (see above if-else)
			sci.add_funding(idea_index, grant_size); // if an idea is found, place in scientist hash map with grant size
		} else {
			if(i <= 3) young_sci.remove(sci);
			else old_sci.remove(sci);
		}
		return grant_size;
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
				if (config.funding) sci.clear_funding();
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
			double tp_returns = sci.returns_tp.stream().mapToDouble(i -> i).sum(); // sum of sci.returns_tp, returns across all ideas in this tp
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