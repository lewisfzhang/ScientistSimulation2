import java.util.*;

public class Scientist {
    public Model model;
    public int age = 0; // SCALAR: age of the given scientists, initiated at 0 years when entered into model

    // SCALAR: multiplier determining scientist specific K (idea K * learning speed = specific k)
    // learning speed is CONSTANT for all ideas for a scientist --> 'lam' centered around 1
    public double learning_speed;

    public double idea_max_mult;
    public double idea_sds_mult;
    public double idea_mean_mult;

    public int start_effort;
    public int avail_effort;
    public HashMap<String, ArrayList<Double>> perceived_rewards;

    // data collection: creates lists to track investment within and across time periods
    public ArrayList<Integer> idea_eff_tp = new ArrayList<>(); // tracks the effort to be invested across different ideas within time period
    public ArrayList<Integer> idea_eff_tot = new ArrayList<>(); // tracks the total effort invested in each idea by the scientist
    // k_paid: 0 = haven't learned, 1 = already paid learning cost
    public ArrayList<Integer> ideas_k_paid_tp = new ArrayList<>(); // records which ideas the scientist paid investment cost for this period
    public ArrayList<Integer> ideas_k_paid_tot = new ArrayList<>(); // records which ideas the scientist has paid the investment cost for overall
    public ArrayList<Integer> returns_tp = new ArrayList<>(); // tracks the returns by idea within time period for the scientist
    public ArrayList<Integer> returns_tot = new ArrayList<>(); // records the sum of returns the scientist has accrued for each idea
    public ArrayList<Integer> overall_returns_tp = new ArrayList<>(); // tracks returns by tp


    public Scientist(Model model) {
        this.model = model;
        this.learning_speed = Functions.poisson(10 * model.learning_rate_mean) / 10.0;

        this.idea_max_mult = Functions.get_random_double(0.5, 1.5, model.config)

    }

    public void step() {
    	reset_trackers();

    	// scientist is still active
        if (this.age < this.model.tp_alive) {
            this.avail_effort = this.start_effort; // reset avail_effort each time period

            HashMap<String, ArrayList<Integer>> inv_dict = Optimize.investing_helper(this);

            update_trackers(inv_dict);
        }
    }

    // reset time period trackers to all zeros
    public void reset_trackers() {
        this.idea_eff_tp = new ArrayList<Integer>(Collections.nCopies(this.idea_eff_tp.size(), 0));
        this.ideas_k_paid_tp = new ArrayList<Integer>(Collections.nCopies(this.ideas_k_paid_tp.size(), 0));
        this.returns_tp = new ArrayList<Integer>(Collections.nCopies(this.returns_tp.size(), 0));
    }

    public void update_trackers(HashMap<String, ArrayList<Integer>> inv_dict) {
        // loop through all investments made within time period
        for (int idx=0; idx<inv_dict.get("idea_idx").size(); idx++) {
            int idea_index = inv_dict.get("idea_idx").get(idx);
            Functions.arr_increment_int(this.idea_eff_tp, idea_index, inv_dict.get("marg_eff").get(idx)); // update this period marginal effort per idea
            Functions.arr_increment_int(this.ideas_k_paid_tp, idea_index, inv_dict.get("k_paid").get(idx)); // update which ideas had investment costs paid IN THIS TP
        }

        // updates "tot"/across time variables with data from corresponding tp variables
        for (int idx=0; idx<this.idea_eff_tp.size(); idx++) {
            int val = this.idea_eff_tp.get(idx);
            Functions.arr_increment_int(this.idea_eff_tot, idx, val);
        }
        for (int idx=0; idx<this.ideas_k_paid_tp.size(); idx++) {
            int val = this.ideas_k_paid_tp.get(idx);
            Functions.arr_increment_int(this.ideas_k_paid_tot, idx, val);
        }
    }
}