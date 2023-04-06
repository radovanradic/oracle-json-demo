CREATE OR REPLACE JSON DUALITY VIEW STUDENT_SCHEDULE AS
SELECT JSON{'student'  : s.name WITH UPDATE,
            'studentId': s.id,
            'schedule': [SELECT JSON {'class': (SELECT JSON{
                                                    'name': c.name,
                                                    'classID': c.id,
                                                    'time': c.time WITH UPDATE,
                                                    'room': c.room,
                                                    'teacher': (SELECT JSON{'teachID': t.id, 'teacher': t.name} FROM teacher t WHERE t.id = c.teacher_id)
                                                } FROM class c WITH UPDATE WHERE c.id = ssch.class_id),
                                      'id': ssch.id
                                      } FROM student_classes ssch WITH INSERT WHERE ssch.student_id = s.id]
            } FROM student s WITH INSERT;