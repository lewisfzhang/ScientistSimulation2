import java.util.*;
import java.lang.Object;
import java.lang.Math;
import java.util.Random;

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
	
	public ArrayList<Scientist> scientist_list;
	public ArrayList<Idea> idea_list;
	
	public Model(Config config) {
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
    		idea.idea_mean = getRandomNumber(idea_mean);
    		idea.idea_max = getRandomNumber(idea_max);
    		idea.idea_k = getRandomNumber(k_mean);
    		idea.total_effort = 0;
    		idea.num_k = 0;n
    		idea_list.add(idea);
    		return ideas_last_tp;
    	}
    }
    
    public void set_perceived_rewards(int ideas_last_tp) {
    	for(int i = 0; i < scientist_list.size() + 1; i++) {
    		Scientist scientist = scientist_list.get(i);
    		for(int j = 0; j < idea_list.size() + 1; j++) {
    			
    		}
    	}
    }
    
    public void update_objects() {
    	
    }
    
    public void pay_out_returns() {
    	
    }
    
    // Generates a random number within a range of [0, max]
    public int getRandomNumber(int max) {
		double x = Math.random();
		int min = 0;
		int num = (int)(x * ((max - min) + 1)) + min;
		return num;
	}
}
