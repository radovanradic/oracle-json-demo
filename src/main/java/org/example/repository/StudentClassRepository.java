package org.example.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.example.entity.StudentClass;

@JdbcRepository(dialect = Dialect.ORACLE)
public interface StudentClassRepository extends PageableRepository<StudentClass, Long> {
}
