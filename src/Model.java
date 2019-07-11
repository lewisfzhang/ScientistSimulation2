import java.util.*;
import java.lang.Object;
import java.util.Random;

public class Model {
    
	public Model(Config config) {
		num_sci = config.sci_rate;
		time_periods = config.time_periods;
		ideas_per_time = config.ideas_per_time;
		tp_alive = config.tp_alive;
		k_mean = config.k_mean;
    }
	
	public ArrayList<Scientist> scientists;
    
	public ArrayList<Idea> ideas;
	
	public int num_sci;
	public int time_periods;
	public int ideas_per_time;
	public int tp_alive;
	public int k_mean;
	
    public void step() {
    	for(int i = 0; i < num_sci; i++) {
    		Scientist scientist = new Scientist();
    	}
    }
}
