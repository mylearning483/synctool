public class MonotonicFetcher implements Fetcher<Long> {

    private long counter = 0;

    @Override
    public Long fetch() {
        try {
            Thread.sleep(5000); // Pause for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ++counter; // Return monotonically increasing value
    }
}
