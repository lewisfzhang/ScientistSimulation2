public class Run {
    public static void main(String[] args) {
        Config config = new Config();
        Model model = new Model(config);
        Collect collect = new Collect(model);
        Time tot = new Time("total time");

        for (int i = 0; i < config.time_periods; i++) {
            System.out.printf("%nstep%d", i);
            Time t = new Time("model step");
            model.step();
            t.stop_time();
        }

        Time t = new Time("collecting");
        collect.collect_data();
        t.stop_time();

        tot.stop_time();
    }
}
