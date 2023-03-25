public class MonotonicFetcher implements Fetcher<String> {

    @Override
    public String fetch() throws Exception {
        Thread.sleep(5000);
        return Long.toString(System.currentTimeMillis());
    }
}
