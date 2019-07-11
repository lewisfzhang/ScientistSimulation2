import java.util.*;
import java.lang.Object;
import java.lang.Math;
import java.util.Random;

public class Model {
    
	public Model(Config config) {
		num_sci = config.sci_rate;
		time_periods = config.time_periods;
		ideas_per_time = config.ideas_per_time;
		tp_alive = config.tp_alive;
		idea_mean = config.idea_mean;
		idea_max = config.idea_max;
		start_effort_mean = config.start_effort_mean;
		k_mean = config.k_mean;
		
    }
	
	public ArrayList<Scientist> scientist_list;
    
	public ArrayList<Idea> idea_list;
	
	/* Public variables that can be accessed across methods, updated by parameters from
	 * config (might make sense to just remove the config layer) */
	public int num_sci;
	public int time_periods;
	public int ideas_per_time;
	public int tp_alive;
	public int k_mean;
	public int idea_mean;
	public int idea_max;
	public int start_effort_mean;
	
	// Model Step
    public void step() {
    	age_scientists(); // add one year to every scientist in the list
    	birth_new_scientists(); // birth a new cohort of scientists
    	birth_new_ideas(); // birth a new set of ideas to be added
    	set_perceived_rewards(); // update each scientists reward list
    }
    
    public void age_scientists() {
    	for(int i = 0; i < scientist_list.size() + 1; i++) {
    		Scientist scientist = scientist_list.get(i);
    		scientist.age++;
    	}
    }
    
    public void birth_new_scientists() {
    	for(int i = 0; i < num_sci; i++) {
    		Scientist scientist = new Scientist();
    		scientist.age = 0;
    		// need to figure out inputs to randomize these terms in a coherent way
    		scientist.idea_max_var = getRandomNumber();
    		scientist.idea_mean_var = getRandomNumber();
    		scientist.learning_speed = getRandomNumber();
    		scientist_list.add(scientist);
    	}
    }
    
    public void birth_new_ideas() {
    	for(int i = 0; i < ideas_per_time; i++) {
    		Idea idea = new Idea();
    		idea.idea_mean = getRandomNumber(idea_mean);
    		idea.idea_max = getRandomNumber(idea_max);
    		idea.idea_k = getRandomNumber(k_mean);
    		idea.total_effort = 0;
    		idea.num_k = 0;
    		idea_list.add(idea);
    	}
    }
    
    public void set_perceived_rewards() {
    	for(int i = 0; i < scientist_list.size() + 1; i++) {
    		Scientist scientist = scientist_list.get(i);
    		for(int j = 0; j < idea_list.size() + 1; j++) {
    			
    		}
    	}
    }
    
    // Generates a random number within a range of [0, max]
    public int getRandomNumber(int max) {
		double x = Math.random();
		int min = 0;
		int num = (int)(x * ((max - min) + 1)) + min;
		return num;
	}
}
