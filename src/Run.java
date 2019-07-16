public class Run {
    public static void main(String[] args) {
        Config config = new Config();
        Model model = new Model(config);
        Collect collect = new Collect(model);

        for (int i = 0; i < config.time_periods; i++) {
            Time t = new Time("\nmodel step");
            model.step();
            t.stop_time();
        }

        Time t = new Time("\ncollecting");
        collect.collect_data();
        t.stop_time();
    }
}
