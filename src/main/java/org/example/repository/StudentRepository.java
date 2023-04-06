package org.example.repository;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.example.entity.Student;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.ORACLE)
public interface StudentRepository extends PageableRepository<Student, Long> {

    @Join("classes")
    Optional<Student> findByName(String name);
}
