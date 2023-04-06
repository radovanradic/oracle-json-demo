package org.example.repository.view;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.QueryResult;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.JsonDataType;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import org.example.entity.view.StudentView;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.ORACLE)
@QueryResult(type = QueryResult.Type.JSON)
public interface StudentViewRepository extends GenericRepository<StudentView, Long> {

    @Query("SELECT ss.DATA FROM STUDENT_SCHEDULE ss WHERE ss.DATA.student=:name")
    Optional<StudentView> findByName(String name);

    @Query("SELECT ss.DATA FROM STUDENT_SCHEDULE ss WHERE ss.DATA.studentId=:id")
    Optional<StudentView> findById(Long id);

    @Query("INSERT INTO STUDENT_SCHEDULE VALUES (:data)")
    void insert(@TypeDef(type = DataType.JSON) @Parameter("data") StudentView studentView);

    @Query("UPDATE STUDENT_SCHEDULE ss SET ss.data = :data WHERE ss.DATA.studentId = :id")
    void update(@TypeDef(type = DataType.JSON) StudentView data, Long id);

    @Query("UPDATE STUDENT_SCHEDULE ss SET ss.data = :data WHERE ss.DATA.student = :name")
    void updateByName(@TypeDef(type = DataType.JSON) StudentView data, String name);

    @Query("DELETE FROM STUDENT_SCHEDULE ss WHERE ss.DATA.studentId = :id")
    int delete(Long id);

    @Query("DELETE FROM STUDENT_SCHEDULE ss WHERE ss.DATA.student = :name")
    int deleteByName(String name);

    @Query("UPDATE STUDENT_SCHEDULE ss SET ss.DATA = json_transform(DATA, SET '$.student' = :newName) WHERE ss.DATA.student = :oldName")
    void updateName(String oldName, String newName);
}
