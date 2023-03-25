public interface Fetcher<T> {
    T fetch() throws Exception;
}
