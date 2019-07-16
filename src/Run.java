public class run {
    public static void main(String[] args) {
        Config config = new Config();
        Model model = new Model(config);
        Collect collect = new Collect(model);
        for (int t = 0; t < config.time_periods; t++) {
            model.step();
        }
        collect.collect_data();
    }
}
