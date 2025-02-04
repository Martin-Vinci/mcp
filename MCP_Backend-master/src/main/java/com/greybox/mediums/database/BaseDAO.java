package com.greybox.mediums.database;

import java.util.List;

public interface BaseDAO<T, U> {

    T getById(U id);

    T getByCode(U code);

    void delete(U id);

    List<T> getAll();

    T update(T t);

    Integer create(T t);

}
