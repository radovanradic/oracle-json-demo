package org.example;

import io.micronaut.data.exceptions.OptimisticLockException;
import jakarta.inject.Singleton;
import org.example.entity.Class;
import org.example.entity.Student;
import org.example.entity.StudentClass;
import org.example.entity.Teacher;
import org.example.entity.view.StudentScheduleClassView;
import org.example.entity.view.StudentScheduleView;
import org.example.entity.view.StudentView;
import org.example.entity.view.TeacherView;
import org.example.repository.ClassRepository;
import org.example.repository.StudentClassRepository;
import org.example.repository.StudentRepository;
import org.example.repository.TeacherRepository;
import org.example.repository.view.StudentViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class DemoRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoRunner.class);
    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final StudentViewRepository studentViewRepository;

    public DemoRunner(TeacherRepository teacherRepository, ClassRepository classRepository,
                      StudentRepository studentRepository, StudentClassRepository studentClassRepository,
                      StudentViewRepository studentViewRepository) {
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.studentClassRepository = studentClassRepository;
        this.studentViewRepository = studentViewRepository;
    }

    public void runDemo() {
        // Creates initial data into relational tables that will form view results
        initData();

        // Finding by name and updating
        findAndUpdate();
        // Finding by name that does not exist
        findNonExisting();
        // Finds record and updates partial data - class times and student name
        findAndUpdatePartial();
        // Inserts new student and relation with existing class
        insertNew();
        // Delete data via json view
        deleteRecord();
    }

    private void initData() {
        studentClassRepository.deleteAll();
        classRepository.deleteAll();
        teacherRepository.deleteAll();
        studentRepository.deleteAll();

        Teacher teacherAnna = teacherRepository.save(new Teacher("Mrs. Anna"));
        Teacher teacherJeff = teacherRepository.save(new Teacher("Mr. Jeff"));

        Student denis = studentRepository.save(new Student("Denis"));
        Student josh = studentRepository.save(new Student("Josh"));
        Student fred = studentRepository.save(new Student("Fred"));

        Class math = classRepository.save(new Class("Math", "A101", LocalTime.of(10, 00), teacherAnna));
        Class english = classRepository.save(new Class("English", "A102", LocalTime.of(11, 00), teacherJeff));
        Class german = classRepository.save(new Class("German", "A103", LocalTime.of(12, 00), teacherAnna));

        studentClassRepository.save(new StudentClass(denis, math));
        studentClassRepository.save(new StudentClass(josh, math));
        studentClassRepository.save(new StudentClass(fred, math));

        studentClassRepository.save(new StudentClass(denis, german));
        studentClassRepository.save(new StudentClass(josh, english));
        studentClassRepository.save(new StudentClass(fred, german));
    }

    void findAndUpdate() {
        // Test finding data using view from records created in source tables
        String studentName = "Denis";
        Optional<StudentView> optDenisStudentView = studentViewRepository.findByName(studentName);
        boolean found = optDenisStudentView.isPresent();
        if (found) {
            LOG.info("Found student classes for student {}: {}", studentName, optDenisStudentView.get());
        } else {
            LOG.error("Didn't find student classes for student named {}", studentName);
        }

        if (found) {
            // Do the view update by changing class schedule times
            StudentView denisStudentView = optDenisStudentView.get();
            Student student = studentRepository.findByName(denisStudentView.getStudent()).get();
            Map<Long, LocalTime> classSchedule = new HashMap<>();
            for (Class clazz : student.getClasses()) {
                // Keep here to verify update
                classSchedule.put(clazz.getId(), clazz.getTime());
            }

            for (StudentScheduleView schedule : denisStudentView.getSchedule()) {
                // Schedule one hour later
                schedule.getClazz().setTime(schedule.getClazz().getTime().plusHours(1));
            }
            studentViewRepository.updateByName(denisStudentView, denisStudentView.getStudent());
            student = studentRepository.findByName(denisStudentView.getStudent()).get();
            // Validate times are scheduled one hour later
            for (Class clazz : student.getClasses()) {
                LocalTime newClassTime = clazz.getTime();
                LocalTime oldClassTime = classSchedule.get(clazz.getId());
                if (newClassTime.minusHours(1).equals(oldClassTime)) {
                    LOG.info("Updated properly time for class: {}", clazz);
                } else {
                    LOG.error("Failed to update time for class: {}", clazz);
                }
            }
        }
    }

    void findNonExisting() {
        String randomName = UUID.randomUUID().toString();
        Optional<StudentView> optUnexpectedStudent = studentViewRepository.findByName(randomName);
        boolean found = optUnexpectedStudent.isPresent();
        if (found) {
            LOG.error("Found student by random name: {}", randomName);
        } else {
            LOG.info("As expected, non existing student {} not found", randomName);
        }
    }

    void findAndUpdatePartial() {
        String studentName = "Josh";
        Optional<StudentView> optJoshStudentView = studentViewRepository.findByName(studentName);
        boolean found = optJoshStudentView.isPresent();
        if (found) {
            LOG.info("Found student classes for student {}: {}", studentName, optJoshStudentView.get());
        } else {
            LOG.error("Didn't find student classes for student named {}", studentName);
        }
        // Test updating single field
        if (found) {
            // Let's rename the student
            String newStudentName = "New Josh";
            studentViewRepository.updateName(studentName, newStudentName);
            if (studentRepository.findByName(studentName).isPresent()) {
                LOG.error("Student name {} should have been update to {}", studentName, newStudentName);
            } else {
                LOG.info("Successfully changed student name {} to new name {}", studentName, newStudentName);
            }

            // Try to trigger optimistic lock exception with invalid ETAG
            StudentView newJoshStudentView = studentViewRepository.findByName(newStudentName).get();
            if (newJoshStudentView == null) {
                LOG.error("Couldn't find student by name {}", newStudentName);
            } else {
                newJoshStudentView.getMetadata().setEtag(UUID.randomUUID().toString());
                boolean error = false;
                try {
                    studentViewRepository.update(newJoshStudentView, newJoshStudentView.getStudentId());
                } catch (OptimisticLockException e) {
                    error = true;
                }
                if (!error) {
                    LOG.error("Should get OptimisticLockException error when updating with wrong ETAG");
                } else {
                    LOG.info("As expected OptimisticLockException was thrown when updating with unexpected ETAG value");
                }
            }
        }
    }

    void insertNew() {
        // Test inserting into the view
        StudentView ivoneStudentView = new StudentView();
        String studentName = "Ivone";
        ivoneStudentView.setStudent(studentName);
        StudentScheduleView newStudentScheduleView = new StudentScheduleView();

        String teacherName = "Mrs. Anna";
        Teacher teacherAnna = teacherRepository.findByName(teacherName);
        String className = "Math";
        TeacherView teacherView = new TeacherView();
        teacherView.setTeacher(teacherAnna.getName());
        teacherView.setTeachID(teacherAnna.getId());

        Class classMath = classRepository.findByName(className);
        StudentScheduleClassView studentScheduleClassView = new StudentScheduleClassView();
        // By inserting new student class, we can also update class time as class is marked as updatable in the view
        LocalTime classTime = classMath.getTime();
        studentScheduleClassView.setTime(classTime.plusMinutes(30));
        studentScheduleClassView.setName(classMath.getName());
        studentScheduleClassView.setClassID(classMath.getId());
        studentScheduleClassView.setRoom(classMath.getRoom());
        studentScheduleClassView.setTeacher(teacherView);

        newStudentScheduleView.setClazz(studentScheduleClassView);
        ivoneStudentView.setSchedule(List.of(newStudentScheduleView));
        studentViewRepository.insert(ivoneStudentView);

        Optional<StudentView> optIvoneStudentView = studentViewRepository.findByName(studentName);
        boolean found = optIvoneStudentView.isPresent();
        if (found) {
            LOG.info("Successfully saved student class for {}: {}", studentName, optIvoneStudentView.get());
            // And just to validate that saved local time is + 30 minutes from initial class time
            LocalTime studentClassTime = optIvoneStudentView.get().getSchedule().get(0).getClazz().getTime();
            if (classTime.plusMinutes(30).equals(studentClassTime)) {
                LOG.info("Properly updated class time: {}", studentClassTime);
            } else {
                LOG.error("Class time not update as expected: {}", studentClassTime);
            }
            // And also in class table itself
            Class clazz = classRepository.findByName(className);
            LocalTime updatedClassTime = clazz.getTime();
            if (classTime.plusMinutes(30).equals(updatedClassTime)) {
                LOG.info("Properly updated class time: {}", updatedClassTime);
            } else {
                LOG.error("Class time not update as expected: {}", updatedClassTime);
            }
        } else {
            LOG.error("Failed to save student class for {}", studentName);
        }
    }

    void deleteRecord() {
        String studentName = "Ivone";
        Optional<StudentView> optionalStudentView = studentViewRepository.findByName(studentName);
        boolean found = optionalStudentView.isPresent();
        if (found) {
            studentViewRepository.deleteByName(studentName);
            optionalStudentView = studentViewRepository.findByName(studentName);
            if (optionalStudentView.isPresent()) {
                LOG.error("Student with name {} has not been deleted when it was expected", studentName);
            } else {
                LOG.info("Student with name {} was successfully deleted", studentName);
            }
            // Verify via regular repo
            Optional<Student> optionalStudent = studentRepository.findByName(studentName);
            if (optionalStudent.isPresent()) {
                LOG.error("Student with name {} has not be deleted when it was expected. Tested using studentRepository", studentName);
            } else {
                LOG.info("Student with name {} was successfully deleted. Tested using studentRepository", studentName);
            }
        } else {
            LOG.error("Student with name {} not found", studentName);
        }
    }
}
