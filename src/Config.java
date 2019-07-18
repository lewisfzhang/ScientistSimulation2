import java.util.Random;

class Config {
    public int seed = 123;
    public int[] seed_array;
    public int next_seed_idx;

    public int time_periods = 20;
    public int ideas_per_time = 10;
    public int sci_rate = 10;
    public int tp_alive = 8;

    // related to idea
    public int idea_mean = 300;
    public int idea_sds = (int) (0.25 * idea_mean); // 75
    public int idea_max = 100;
    public int k_mean = (int) ((1.0/8) * idea_mean); // 37

    // related to scientist
    public int start_effort_mean = (int) (0.5 * idea_mean); // 150
    public int learning_rate_mean = 1;
    public int discov_rate_mean = (int) (0.5 * ideas_per_time); // 5

    public boolean equal_returns = true;

    public static int max_weight = 3;

    public Config() {
        set_seed();
    }

    public void set_seed() {
        Random r = new Random();
        r.setSeed(seed);
        this.seed_array = new int[10000000]; // initialize 10000000 random seeds
        for (int i = 0; i < this.seed_array.length; i++) {
            this.seed_array[i] = r.nextInt();
        }
        this.next_seed_idx = 0; // keeps track of the last seed that was used
    }

    public int get_next_seed() {
        this.next_seed_idx++;
        if (this.next_seed_idx == this.seed_array.length) {
            this.next_seed_idx = 0; // loop back in the seed array
        }
        return this.seed_array[this.next_seed_idx];
    }
}