package org.example.repository.view;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.example.entity.view.StudentView;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.ORACLE)
public interface StudentViewRepository extends PageableRepository<StudentView, Long> {

    Optional<StudentView> findByStudent(String student);

    void updateStudentByStudentId(@Id Long studentId, String student);
}
