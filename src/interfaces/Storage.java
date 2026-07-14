package interfaces;

public interface Storage {
    void save(List<Transaction> transactions, String path) throws Exception;

    List<Transaction> load(String path) throws Exception;
}
