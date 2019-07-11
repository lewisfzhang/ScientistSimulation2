public class Run {
    public static void main(String[] args) {
        Config config = new Config();
        Model model = new Model(config);

        for (int i=0; i<config.time_periods; i++) {
            model.step();
        }
    }
}
