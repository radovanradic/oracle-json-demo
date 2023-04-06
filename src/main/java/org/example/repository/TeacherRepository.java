package org.example.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.example.entity.Teacher;

@JdbcRepository(dialect = Dialect.ORACLE)
public interface TeacherRepository extends PageableRepository<Teacher, Long> {

    Teacher findByName(String name);
}
