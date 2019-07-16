import java.util.ArrayList;

public class Config {
    
	public int seed = 123; // keeping variation constant
	public ArrayList<Integer> seed_array = null;
	public int next_seed_index = 0;
	// set seed
	
    public int time_periods = 20; // number of stable time_periods in the model
    public int ideas_per_time = 10; // number of ideas unique to each time period
    public int sci_rate = 10; // number of scientists born per time period
    public int tp_alive = 8; // the number of TP a scientist can actively invest in ideas

    // maybe think about relating the following variables together by proportion
    public int idea_mean = 300;
    public int idea_sds = 75;
    public int idea_max = 100;
    public int start_effort_mean = 150;
    public int k_mean = 37;
    public int learning_rate_mean = 1;

    public boolean equal_returns = true;
    public int opt_num = 0;
    
}
