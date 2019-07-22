import java.lang.Math;
import java.util.ArrayList;

public class Model {
	// initiates the key parameters within the model, as set in config
	public Config config;

	// SCALARS
	public int num_sci;
	public int time_periods;
	public int ideas_per_time;
	public int tp_alive;
	public int k_mean;
	public int idea_mean;
	public int idea_max;
	public int idea_sds;
	public int start_effort_mean;
	public int learning_rate_mean;
	public int discov_rate;
	public int tp = 0;

	// ARRAYS: creates empty arraylists to track scientists and ideas; index indicates age
	public ArrayList<Scientist> scientist_list = new ArrayList<>();
	public ArrayList<Idea> idea_list = new ArrayList<>();

	// model constructor
	public Model(Config config) {
		this.config = config;
		num_sci = config.sci_rate;
		time_periods = config.time_periods;
		ideas_per_time = config.ideas_per_time;
		idea_mean = config.idea_mean;
		idea_max = config.idea_max;
		idea_sds = config.idea_sds;
		tp_alive = config.tp_alive;
		start_effort_mean = config.start_effort_mean;
		k_mean = config.k_mean;
		learning_rate_mean = config.learning_rate_mean;
		discov_rate = config.discov_rate;
    }
	
	// defines the process for one time period within the model
    public void step() {
    	age_scientists();
    	birth_new_scientists();

		int ideas_last_tp = birth_new_ideas(); // keep track of how many old ideas so we only have to update new ideas
		update_scientist_idea(ideas_last_tp);

    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		sci.step();
    	}

    	update_objects();
    	pay_out_returns();

    	tp++;
    }

    // adds one year to the age of every scientist that already exists within the model
    public void age_scientists() {
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		sci.age++;
    	}
    }

    // creates new scientists, birthed at age 0, and sets their random constants (variances, learning speed, and effort)
	public void birth_new_scientists() {
    	for(int s = 0; s < num_sci; s++) {
    		Scientist sci = new Scientist(this);
    		scientist_list.add(sci);
    	}
    }

    // creates new ideas and sets their random constants (true mean, true max, investment cost)
	// returns the number of ideas from last tp
	public int birth_new_ideas() {
    	for(int i = 0; i < ideas_per_time; i++) {
    		Idea idea = new Idea(this);
    		idea_list.add(idea);
    	}
		// return should be 0 if tp = 0 because there are no previous ideas before tp = 0 (see set_perceived_rewards)
    	return (tp == 0) ? 0 : idea_list.size() - ideas_per_time;
    }

    // loop through every scientist, appending their perceived rewards dataframe with new ideas
	// also updates related list with extra spots for new ideas --> append_scientist_lists
	public void update_scientist_idea(int ideas_last_tp) {
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
    		
    		// check the previous period to see which newly discovered ideas had their entry costs paid
    		for(int i = 0; i < sci.ideas_k_paid_tot.size(); i++) {
    			if(sci.discov_ideas.get(i) == 1 & sci.ideas_k_paid_tot.get(i) == 0)
    				sci.discov_ideas.set(i, 0);
    		}
    		
    		// determine which ideas a scientist will discover
			int count = 0;
			if(sci.age == 0) {
				while (count < 2 * sci.discov_rate) {
        			int new_idea_idx = Functions.get_random_int(0, idea_list.size(), config);
        			if (sci.discov_ideas.get(new_idea_idx) == 0) { // only update if idea hasn't been discovered
    					sci.discov_ideas.set(new_idea_idx, 1);
    					count++;
    				}
    			}
			}
			if(sci.age != 0) {
    			while (count < sci.discov_rate) {
        			int new_idea_idx = Functions.get_random_int(0, idea_list.size(), config);
        			if (sci.discov_ideas.get(new_idea_idx) == 0) { // only update if idea hasn't been discovered
    					sci.discov_ideas.set(new_idea_idx, 1);
    					count++;
    				}
    			}
    		}
    	}
    }

	// updates the lists within each scientist object to reflect the correct number of available ideas
    // ignore static warning, only because we aren't using self keyword
	// keep it in model since it is called by the model step function --> set_perceived_rewards()
	public void append_scientist_lists(Scientist sci) {
    	sci.idea_eff_tp.add(0.0);
        sci.idea_eff_tot.add(0.0);
        sci.ideas_k_paid_tp.add(0);
        sci.ideas_k_paid_tot.add(0);
		sci.discov_ideas.add(0);
		sci.returns_tp.add(0.0);
        sci.returns_tot.add(0.0);
    }

    // data collection: loop through each idea object, updating the effort that was invested in this time period
	public void update_objects() {
    	for(int idx = 0; idx < idea_list.size(); idx++) {
    		Idea idea = idea_list.get(idx);
    		int effort_invested_tp = 0;
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
	public void pay_out_returns() {
    	for(int i = 0; i < idea_list.size(); i++) {
    		Idea idea = idea_list.get(i);
    		if(idea.effort_by_tp.get(tp) != 0) {
    			int start_effort = idea.total_effort - idea.effort_by_tp.get(tp); // since we already updated effort_by_tp in update_objects()
    			int end_effort = idea.total_effort;
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
	public void process_winners(int iidx, double idea_returns) {
    	ArrayList<Integer> list_of_investors = new ArrayList<Integer>();
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
