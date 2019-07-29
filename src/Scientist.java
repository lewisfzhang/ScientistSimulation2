import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

class Scientist {
    Model model;
    int id;
    int age = 0; // SCALAR: age of the given scientists, initiated at 0 years when entered into model
    int tp_alive;

    // SCALAR: multiplier determining scientist specific K (idea K * learning speed = specific k)
    // learning speed is CONSTANT for all ideas for a scientist --> 'lam' centered around 1
    double learning_speed;

    // Logic: some scientists will be more optimistic than others
    double idea_max_mult; // SCALAR: multiplier determining max perceived returns
    double idea_sds_mult; // SCALAR: multiplier determining sds of perceived returns
    double idea_mean_mult; // SCALAR: multiplier determining perceived lambda

    double start_effort; // SCALAR: determines starting effort for a scientist in all periods
    double avail_effort; // SCALAR: counter that determines how much effort a scientist has left to allocate within TP
    double decay_rate; // SCALAR: rate of age decay, slowing down effort available for scientist in each TP
    HashMap<String, ArrayList<Double>> perceived_rewards; // tracks perceived rewards

    // used in optimization
    ArrayList<Double> marg_eff;

    // data collection: creates lists to track investment within and across time periods
    ArrayList<Double> idea_eff_tp = new ArrayList<>(); // tracks the effort to be invested across different ideas within time period
    ArrayList<Double> idea_eff_tot = new ArrayList<>(); // tracks the total effort invested in each idea by the scientist

    // k_paid: 0 = haven't learned, 1 = already paid learning cost
    ArrayList<Integer> ideas_k_paid_tp = new ArrayList<>(); // records which ideas the scientist paid investment cost for this period
    ArrayList<Integer> ideas_k_paid_tot = new ArrayList<>(); // records which ideas the scientist has paid the investment cost for overall

    ArrayList<Integer> discov_ideas = new ArrayList<>(); // 1 = discovered, 0 = not discovered
    double discov_rate; // the rate that scientists discover new ideas, double converted to int when we calculate tru_rate in model.update_sci_idea()
        
    ArrayList<Double> returns_tp = new ArrayList<>(); // tracks the returns by idea within time period for the scientist
    ArrayList<Double> returns_tot = new ArrayList<>(); // records the sum of returns the scientist has accrued for each idea
    ArrayList<Double> overall_returns_tp = new ArrayList<>(); // tracks returns by tp


    Scientist(Model model) {
        this.model = model;
        id = model.scientist_list.size(); // Scientist object is created before appending to list --> get current list size before append as idx
        tp_alive = model.config.tp_alive; // Functions.poisson(model.config.tp_alive, model.config); // scientists have different life spans

        learning_speed = Functions.poisson(10 * model.config.learning_rate_mean, model.config) / 10.0;
        discov_rate = Functions.get_random_double(model.config.discov_rate_min, model.config.discov_rate_max, model.config);

        idea_max_mult = Functions.get_random_double(0.5, 1.5, model.config);
        idea_sds_mult = Functions.get_random_double(0.5, 1.5, model.config);
        idea_mean_mult = Functions.get_random_double(0.5, 1.5, model.config);

        start_effort = Functions.poisson(model.config.start_effort_mean, model.config);
        decay_rate = (1 - model.config.decay_prop) * start_effort / tp_alive;

        perceived_rewards = new HashMap<>();
        perceived_rewards.put("Idea Mean", new ArrayList<>()); // all of the arraylists are Double type
        perceived_rewards.put("Idea SDS", new ArrayList<>());
        perceived_rewards.put("Idea Max", new ArrayList<>());
        perceived_rewards.put("Idea K", new ArrayList<>());
    }

    void step() {
    	reset_trackers();

    	// scientist is still active
        if (age < tp_alive) {
            avail_effort = start_effort - age * decay_rate; // reset avail_effort each time period, accounting for age decay

            HashMap<String, ArrayList<Double>> inv_dict;
            if (model.config.smart_opt) {inv_dict = new Smart_Optimize().investing_helper(this);}
            else {inv_dict = Optimize.investing_helper(this);}

            update_trackers(inv_dict);
        }
    }

    // reset time period trackers to all zeros
    void reset_trackers() {
        idea_eff_tp = new ArrayList<>(Collections.nCopies(idea_eff_tp.size(), 0.0));
        ideas_k_paid_tp = new ArrayList<>(Collections.nCopies(ideas_k_paid_tp.size(), 0));
        returns_tp = new ArrayList<>(Collections.nCopies(returns_tp.size(), 0.0));
    }

    void update_trackers(HashMap<String, ArrayList<Double>> inv_dict) {
        // loop through all investments made within time period
        for (int idx=0; idx<inv_dict.get("idea_idx").size(); idx++) {
            double idea_index = inv_dict.get("idea_idx").get(idx);
            int idea_idx = (int) idea_index;
            double k_paid_increment = inv_dict.get("k_paid").get(idx);
            int k_paid_inc = (int) k_paid_increment;
            Functions.arr_increment_double(idea_eff_tp, idea_idx, inv_dict.get("marg_eff").get(idx)); // update this period marginal effort per idea
            Functions.arr_increment_int(ideas_k_paid_tp, idea_idx, k_paid_inc); // update which ideas had investment costs paid IN THIS TP
        }

        // updates "tot"/across time variables with data from corresponding tp variables
        for (int idx=0; idx<idea_eff_tp.size(); idx++) {
            double val = idea_eff_tp.get(idx);
            Functions.arr_increment_double(idea_eff_tot, idx, val);
        }
        for (int idx=0; idx<ideas_k_paid_tp.size(); idx++) {
            int val = ideas_k_paid_tp.get(idx);
            Functions.arr_increment_int(ideas_k_paid_tot, idx, val);
        }
    }
}