CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW STUDENT_SCHEDULE AS
SELECT JSON{
        'studentId': STUDENT.id,
    'student': STUDENT.name WITH UPDATE,
'schedule': [SELECT JSON{
'id': STUDENT_CLASSES.id,
'class': (SELECT JSON{
'classID': CLASS.id,
'teacher': (SELECT JSON{
'teachID': TEACHER.id,
'teacher': TEACHER.name} FROM TEACHER WITH UPDATE WHERE CLASS.teacher_id = TEACHER.id),
'room': CLASS.room,
'time': CLASS.time,
'name': CLASS.name WITH UPDATE} FROM CLASS WITH UPDATE WHERE STUDENT_CLASSES.class_id = CLASS.id)}
    FROM STUDENT_CLASSES WITH INSERT UPDATE DELETE WHERE STUDENT.id = STUDENT_CLASSES.student_id]} FROM STUDENT WITH UPDATE INSERT DELETE;