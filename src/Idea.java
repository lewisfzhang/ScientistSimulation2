import java.util.ArrayList;
import java.util.Collections;

public class Idea {
    // instrinsic variables
    public Model model;
	public int idea_mean; // aka inflection point
    public int idea_max; // multiple, curve goes from 0 - M
    public int idea_sds; 
    public int idea_k; // entry cost/barrier to enter an idea

    // idea collectors
    public int total_effort; // total effort invested in idea to date, also accessed by optimization algorithms
    public ArrayList<Integer> effort_by_tp; // total effort invested in idea by period
    public ArrayList<Integer> num_k_by_tp; // number people who paid investment cost by period

    public Idea(Model model) {
    	this.model = model;
    	idea_mean = Functions.poisson(model.idea_mean);
    	idea_max = Functions.poisson(model.idea_max);
    	idea_sds = Functions.poisson(model.idea_sds);
    	idea_k = Functions.poisson(model.k_mean);

        total_effort = 0;
        effort_by_tp = new ArrayList<Integer>(Collections.nCopies(model.time_periods, 0));
        num_k_by_tp = new ArrayList<Integer>(Collections.nCopies(model.time_periods, 0));
    }

    public static double get_returns(int means, int sds, int max, int start_idx, int end_idx) {
        double start = max * logistic_cdf(start_idx, means, sds);
        double end = max * logistic_cdf(end_idx, means, sds);
        return end - start;
    }

    public static double old_logistic_cdf(int x, int loc, int scale) {
        return 1 / (1 + Math.exp((loc - x) / (double) scale));
    }

    public static double logistic_cdf(int x, int loc, int scale) {
        return (old_logistic_cdf(x, loc, scale) - old_logistic_cdf(0, loc, scale)) / (1 - old_logistic_cdf(0, loc, scale));
    }
}
