package org.example.entity.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.JsonView;

import java.util.List;

@JsonView(value = "STUDENT_SCHEDULE")
public class StudentView {
        @Id
        @GeneratedValue(GeneratedValue.Type.IDENTITY)
        private Long studentId;
        private String student;
        private List<StudentScheduleView> schedule;
        @JsonProperty("_metadata")
        private Metadata metadata;

        public Long getStudentId() {
                return studentId;
        }

        public void setStudentId(Long studentId) {
                this.studentId = studentId;
        }

        public String getStudent() {
                return student;
        }

        public void setStudent(String student) {
                this.student = student;
        }

        public List<StudentScheduleView> getSchedule() {
                return schedule;
        }

        public void setSchedule(List<StudentScheduleView> schedule) {
                this.schedule = schedule;
        }

        public Metadata getMetadata() {
                return metadata;
        }

        public void setMetadata(Metadata metadata) {
                this.metadata = metadata;
        }

        @Override
        public String toString() {
                return "Student{" +
                        "studentId=" + studentId +
                        ", student='" + student + '\'' +
                        ", schedule=" + schedule +
                        '}';
        }
}