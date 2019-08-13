import java.util.HashMap;
import java.util.Random;

class Config implements java.io.Serializable {
    String parent_dir;

    int seed = 123;
    int[] seed_array;
    int next_seed_idx;

    int time_periods = 20;
    int ideas_per_time = 10;
    int sci_rate = 10;
    int tp_alive = 8;

    // related to idea
    int idea_mean = 300;
    int idea_sds = (int) (0.25 * idea_mean); // 75
    int idea_max = 100;
    int k_mean = (int) ((1.0/8) * idea_mean); // 37

    // related to scientist
    int start_effort_mean = (int) (0.5 * idea_mean); // 150
    double decay_prop = 0.2; // by the time a scientist retires, they should have 80% of their starting available effort
    int learning_rate_mean = 1;
    int discov_rate_min = (int) (0.2 * ideas_per_time); // 2
    int discov_rate_max = (int) (0.4 * ideas_per_time); // 4

    // related to funding
    double budget_prop = 0.25; // total_budget = budget_proportion * number_scientists * start_effort_mean
    double e_grant_size_prop = 0.05;
    double y_k_n = 0.125;
    double y_k_b = 0.125;
    double y_e_n = 0.125;
    double y_e_b = 0.125;
    double o_k_n = 0.125;
    double o_k_b = 0.125;
    double o_e_n = 0.125;
    double o_e_b = 0.125;
    HashMap<Integer, Double> grant_buckets = new HashMap<>();
    
    boolean funding = true;
    boolean equal_returns = true;
    boolean smart_opt = true;

    static int max_weight = 3; // for branch tree purposes

    Config() {
        get_path();
        set_seed();

        if (funding) { // update grant buckets
            grant_buckets.put(1, y_k_n);
            grant_buckets.put(2, y_k_b);
            grant_buckets.put(3, y_e_n);
            grant_buckets.put(4, y_e_b);
            grant_buckets.put(5, o_k_n);
            grant_buckets.put(6, o_k_b);
            grant_buckets.put(7, o_e_n);
            grant_buckets.put(8, o_e_b);
        }
    }

    void get_path() {
        String s = getClass().getName();
        int i = s.lastIndexOf(".");
        if(i > -1) s = s.substring(i + 1);
        s = s + ".class";
        // System.out.println("name " +s);
        String testPath = this.getClass().getResource(s).toString();
        parent_dir = testPath.substring(5, testPath.length() - s.length() - 37);
        // System.out.println(parent_dir);
    }

    void set_seed() {
        Random r = new Random();
        r.setSeed(seed);
        this.seed_array = new int[10000000]; // initialize 10000000 random seeds
        for (int i = 0; i < this.seed_array.length; i++) {
            this.seed_array[i] = r.nextInt();
        }
        this.next_seed_idx = 0; // keeps track of the last seed that was used
    }

    int get_next_seed() {
        this.next_seed_idx++;
        if (this.next_seed_idx == this.seed_array.length) {
            this.next_seed_idx = 0; // loop back in the seed array
        }
        return this.seed_array[this.next_seed_idx];
    }
}