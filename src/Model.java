import java.util.*;
import java.lang.Object;
import java.lang.Math;
import java.util.Random;
import java.util.ArrayList;

public class Model {
    
	/* Public variables that can be accessed across methods, updated by parameters from
	 * config (might make sense to just remove the config layer) */
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
	public int tp = 0;
	public int ideas_last_tp;
	public boolean equal_returns;
	public Config config;
	
	public ArrayList<Scientist> scientist_list;
	public ArrayList<Idea> idea_list;
	
	public Model(Config config) {
		this.config = config;
		num_sci = config.sci_rate;
		time_periods = config.time_periods;
		ideas_per_time = config.ideas_per_time;
		tp_alive = config.tp_alive;
		idea_mean = config.idea_mean;
		idea_max = config.idea_max;
		idea_sds = config.idea_sds;
		start_effort_mean = config.start_effort_mean;
		k_mean = config.k_mean;
		learning_rate_mean = config.learning_rate_mean;
		equal_returns = config.equal_returns;
    }
	
	// Model Step
    public void step() {
    	age_scientists(); // add one year to every scientist in the list
    	birth_new_scientists(); // birth a new cohort of scientists
    	ideas_last_tp = birth_new_ideas(); // birth a new set of ideas to be added
    	set_perceived_rewards(ideas_last_tp); // update each scientists reward list
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		sci.step();
    	}
    	update_objects();
    	pay_out_returns();
    	tp++;
    }
    
    public void age_scientists() {
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		sci.age++;
    	}
    }
    
    public void birth_new_scientists() {
    	for(int s = 0; s < num_sci; s++) {
    		Scientist sci = new Scientist();
    		// need to make sure the new scientist objects initialize correctly in scientist class
    		scientist_list.add(sci);
    	}
    }
    
    public int birth_new_ideas() {
    	for(int i = 0; i < ideas_per_time; i++) {
    		Idea idea = new Idea();
    		// need to make sure the new scientist objects initialize correctly in scientist class
    		idea_list.add(idea);
    	}
    	int idx = 0;
    	if(tp == 0) {
    		idx = 0;
    	}
    	else {
    		idx = idea_list.size() - ideas_per_time;
    	}
    	return idx;
    }
    
    public void set_perceived_rewards(int ideas_last_tp) {
    	ArrayList<Idea> new_idea_list = new ArrayList<Idea>();
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		if(sci.age == 0) {
    			 new_idea_list = idea_list;
    		}
    		else {
    			new_idea_list = (ArrayList<Idea>) idea_list.subList(ideas_last_tp, idea_list.size());
    		}
    	}
    	
    	// NEED: help implementing the iteration through new idea lists
    }
    
    public void update_objects() {
    	for(int idx = 0; idx < idea_list.size(); idx++) {
    		Idea idea = idea_list.get(idx);
    		int effort_invested_tp = 0;
    		int k_paid_tp = 0;
    		for(int s = 0; s < scientist_list.size(); s++) {
    			Scientist sci = scientist_list.get(s);
    			effort_invested_tp += sci.idea_eff_tp.get(idx);
    			k_paid_tp += sci.ideas_k_paid_tp.get(idx);
    		}
    		idea.total_effort += effort_invested_tp;
    		idea.effort_by_tp.add(effort_invested_tp);
    		idea.num_k_by_tp.add(k_paid_tp);
    	}
    }
    
    public void pay_out_returns() {
    	for(int i = 0; i < idea_list.size(); i++) {
    		Idea idea = idea_list.get(i);
    		if(idea.effort_by_tp.get(tp) != 0) {
    			int start_effort = idea.total_effort - idea.effort_by_tp.get(tp);
    			int end_effort = idea.total_effort;
    			double idea_returns = idea.get_returns(idea.idea_mean, idea.idea_sds, idea.idea_max, start_effort, end_effort);
    			process_winners(i, idea_returns);
    		}
    	}
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		double tp_returns = 0;
    		for(int r = 0; r < sci.returns_tp.size(); r++) {
    			tp_returns += sci.returns_tp.get(r);
    		}
    		sci.overall_returns_tp.add(tp_returns);
    	}
    }
    
    public void process_winners(int iidx, double idea_returns) {
    	ArrayList<Integer> list_of_investors = new ArrayList<Integer>();
    	for(int s = 0; s < scientist_list.size(); s++) {
    		Scientist sci = scientist_list.get(s);
    		if(sci.idea_eff_tp.get(iidx) != 0) {
    			list_of_investors.add(s);
    		}
    	}
    	if(equal_returns) {
    		int total_effort_invested = 0;
    		for(int idx = 0; idx < list_of_investors.size(); idx++) {
    			int sci_id = list_of_investors.get(idx);
    			Scientist sci = scientist_list.get(sci_id);
    			total_effort_invested += sci.idea_eff_tp.get(iidx);
    		}
    		for(int idx = 0; idx < list_of_investors.size(); idx++) {
    			int sci_id = list_of_investors.get(idx);
    			Scientist sci = scientist_list.get(sci_id);
    			double individual_proportion = sci.idea_eff_tp.get(iidx) / total_effort_invested;
    			double individual_returns = individual_proportion * total_effort_invested;
    			arr_increment_double(sci.returns_tp, iidx, individual_returns);
    			arr_increment_double(sci.returns_tot, iidx, individual_returns);
    		}
    	}
    	else {
    		int oldest_scientist_id = list_of_investors.get(0);
    		Scientist sci = scientist_list.get(oldest_scientist_id);
    		arr_increment_double(sci.returns_tp, iidx, idea_returns);
			arr_increment_double(sci.returns_tot, iidx, idea_returns);
    	}
    }
}
