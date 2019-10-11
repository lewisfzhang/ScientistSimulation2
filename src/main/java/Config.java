import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

class Config implements java.io.Serializable {
    String parent_dir;
    String source_dir;

    boolean equal_returns = true; // scientists each get returns proportional to the effort invested in the idea
    boolean smart_opt = true;
    boolean funding = false; // whether to implement funding in the model run
    boolean use_neural = true; // whether to incorporate neural nets in scientist optimization

    int seed = 123; // ensures that model is consistent run over run, can be changed if want to experiment in new model setting
    int[] seed_array;
    int next_seed_idx;

    int time_periods = 30; // how many model steps will be run through
    int ideas_per_time = 8; // how many new ideas are released into the model
    int sci_rate = 10; // how many new scientists are "born" in each period, impacts model step speed

    // GLOBAL CONSTANTS
    int tp_alive; // average length of life in time periods for scientists in the model; set in "global_config.txt"
    int max_ideas; // handles vector input size for V1 --> scientist action space should not exceed max_ideas --> scientist should not discover more than 20 ideas
    double BETA; // coefficient for NPV, determined by discount rate r

    // related to idea
    int idea_mean = 300; // average lambda (in terms of unit of effort invested) for new idea curves created in model
    int idea_sds = (int) (0.25 * idea_mean); // 75; average SDS for new idea curves created in model
    int idea_max = 100; // average max potential of idea (in terms of scientific impact)
    int k_mean = (int) ((1.0/8) * idea_mean); // 37; average idea specific learning cost

    // related to scientist
    int start_effort_mean = (int) (0.5 * idea_mean); // 150; average effort each scientist can invest per period
    double decay_prop = 0.2; // by the time a scientist retires, they should have 80% of their starting available effort
    int learning_rate_mean = 1; // true learning cost is scientist learning rate * idea specific learning cost; this is average learning rate multiplier for scientists
    int discov_rate_min = (int) (0.125 * ideas_per_time); // 1; minimum number of new ideas discovered per scientist per period
    int discov_rate_max = (int) (0.375 * ideas_per_time); // 3; maximum number of new ideas discovered per scientist per period

    // related to funding
    double budget_prop = 0.05; // total_budget = budget_proportion * number_scientists * start_effort_mean
    double e_grant_size_prop = 0.05; // how many total e grants will be distributed; 100 * prop = number of e grants (20)
    // grant budget proportions; y/o = young/old; k/e = learning vs. total effort grants; n/b = new versus old ideas
    double y_k_n = 0.125;
    double y_k_b = 0.125;
    double y_e = 0.25;
    double o_k_n = 0.125;
    double o_k_b = 0.125;
    double o_e = 0.25;
    HashMap<Integer, Double> grant_buckets = new HashMap<>();

    static int max_weight = 3; // for branch tree purposes

    Config(boolean train_data) {
        get_path();
        load_global_constants();
        set_seed();

        if (train_data) { // train data without funding and neural
           use_neural = false;
           funding = false;
         }

        if (funding) { // update grant buckets
            grant_buckets.put(1, y_k_n);
            grant_buckets.put(2, y_k_b);
            grant_buckets.put(3, y_e);
            grant_buckets.put(4, o_k_n);
            grant_buckets.put(5, o_k_b);
            grant_buckets.put(6, o_e);
        }
    }

    void get_path() { // get working directory path (path to local git repository)
        String s = getClass().getName();
        int i = s.lastIndexOf(".");
        if(i > -1) s = s.substring(i + 1);
        s = s + ".class";
        String testPath = this.getClass().getResource(s).toString();
        int start = testPath.indexOf("/");
        int end = testPath.indexOf("ScientistSimulation2") + "ScientistSimulation2".length();

        parent_dir = testPath.substring(start, end); // from first backslash / up to ...ScientistSimulation2
        source_dir = parent_dir + "/src/main/java/"; // source directory of all Java programs in Maven projects
    }

    void load_global_constants() {
        try {
            Scanner sc = new Scanner(new File(source_dir + "global_config.txt"));
            sc.next();
            tp_alive = sc.nextInt();
            sc.next();
            max_ideas = sc.nextInt();
            sc.next();
            BETA = sc.nextDouble();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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