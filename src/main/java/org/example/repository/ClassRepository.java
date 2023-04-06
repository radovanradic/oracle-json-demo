package org.example.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.example.entity.Class;

@JdbcRepository(dialect = Dialect.ORACLE)
public interface ClassRepository extends PageableRepository<Class, Long> {

    Class findByName(String name);
}
