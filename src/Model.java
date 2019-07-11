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
	
	public ArrayList<Scientist> scientists;
    
	public ArrayList<Idea> ideas;
	
	public int num_sci;
	public int time_periods;
	public int ideas_per_time;
	public int tp_alive;
	public int k_mean;
	public int idea_mean;
	public int idea_max;
	public int start_effort_mean;
	
    public void step() {
    	for(int i = 0; i < num_sci; i++) {
    		Scientist scientist = new Scientist();
    		scientist.idea_max_var = getRandomNumber(idea_max);
    	}
    }
    
    public int getRandomNumber(int average) {
		double x = Math.random();
		int min = 0;
		int max = average * 2;
		int num = (int)(x * ((max - min) + 1)) + min;
		return num;
	}
}
