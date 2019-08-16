import java.util.ArrayList;
import java.util.Collections;

class Idea implements java.io.Serializable {
    // instrinsic variables
    Model model;
    int id;

    // research impact variables
	int idea_mean; // aka inflection point
    int idea_max; // multiple, curve goes from 0 - M
    int idea_sds;
    int idea_k; // entry cost/barrier to enter an idea

    // social impact variables
    int social_mean; // aka inflection point
    int social_max; // multiple, curve goes from 0 - M

    // idea collectors
    double total_effort; // total effort invested in idea to date, also accessed by optimization algorithms
    int num_k_total; // total number of scientists who have invested in idea to date, accessed by smart_opt
    ArrayList<Double> effort_by_tp; // total effort invested in idea by period
    ArrayList<Integer> num_k_by_tp; // number people who paid investment cost by period

    Idea(Model model) {
    	this.model = model;
    	this.id = model.idea_list.size(); // Idea object is created before appending to list --> get current list size before append as idx

    	idea_mean = Functions.poisson(model.config.idea_mean, model.config);
    	idea_max = Functions.poisson(model.config.idea_max, model.config);
    	idea_sds = Functions.poisson(model.config.idea_sds, model.config);
    	idea_k = Functions.poisson(model.config.k_mean, model.config);

    	social_mean = (int) (idea_mean * Functions.get_random_double(1.0, 2.0, model.config)); // social impact must come after research impact, so multiple of 1-2
        social_max = (int) (idea_max * Functions.get_random_double(0.5, 1.5, model.config)); // social impact range based on multiplier of 0.5-1.5 of research impact range

        total_effort = 0;
        num_k_total = 0;
        effort_by_tp = new ArrayList<Double>(Collections.nCopies(model.tp, 0.0));
        num_k_by_tp = new ArrayList<Integer>(Collections.nCopies(model.tp, 0));
    }

    // STATIC: helper functions for calculating idea curve
    static double get_returns(double means, double sds, double max, double start_idx, double end_idx) {
        double start = max * logistic_cdf(start_idx, means, sds);
        double end = max * logistic_cdf(end_idx, means, sds);
        return end - start;
    }

    static double old_logistic_cdf(double x, double loc, double scale) {
        return 1 / (1 + Math.exp((loc - x) / (double) scale));
    }

    // normalizing so that all idea curves start at (0,0)
    static double logistic_cdf(double x, double loc, double scale) {
        return (old_logistic_cdf(x, loc, scale) - old_logistic_cdf(0, loc, scale)) / (1 - old_logistic_cdf(0, loc, scale));
    }
    
    public int phase() {
    	int phase_int;

    	if (this.total_effort < (this.idea_mean - this.idea_sds)) phase_int = 0;
    	else if (this.total_effort > (this.idea_mean + this.idea_sds)) phase_int = 2;
    	else phase_int = 1;

    	return phase_int;
    }
}
