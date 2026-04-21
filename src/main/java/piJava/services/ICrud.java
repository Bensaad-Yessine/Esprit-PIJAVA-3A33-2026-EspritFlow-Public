package piJava.services;

import java.sql.SQLException;
import java.util.List;

public interface ICrud<T> {
    List<T> show() throws SQLException;
    void add(T t) throws SQLException;
    void delete(int id) throws SQLException;
    void edit(T t) throws SQLException;
}