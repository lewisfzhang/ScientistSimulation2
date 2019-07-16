import java.util.Random;

class Config {
    public int seed = 123;
    public int[] seed_array;
    public int next_seed_idx;

    public int time_periods = 20;
    public int ideas_per_time = 10;
    public int sci_rate = 10;
    public int tp_alive = 8;

    // maybe make some of these proportions relative to the idea mean ?
    public int idea_mean = 300;
    public int idea_sds = 75;
    public int idea_max = 100;
    public int start_effort_mean = 150;
    public int k_mean = 37;
    public int learning_rate_mean = 1;

    public boolean equal_returns = true;

    public Config() {
        set_seed();
    }

    public void set_seed() {
        Random r = new Random();
        r.setSeed(seed);
        this.seed_array = new int[10000000];
        for (int i = 0; i < this.seed_array.length; i++) {
            this.seed_array[i] = r.nextInt();
        }
    }

    public int get_next_seed() {
        this.next_seed_idx++;
        if (this.next_seed_idx == this.seed_array.length) {
            this.next_seed_idx = 0; // loop back in the seed array
        }
        return this.seed_array[this.next_seed_idx];
    }
}