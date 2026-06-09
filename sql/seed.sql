USE document_system;

-- 普通用户：2016001 / 123456
INSERT INTO b_user (username, password, salt, real_name, id_card, gender, birthday, education, phone, identity, role, status)
SELECT '2016001',
       MD5(CONCAT('123456', 'abc123456789012')),
       'abc123456789012',
       'zhangsan',
       '421010199601191111',
       1,
       '1996-01-13',
       'zhuanke',
       '18909786756',
       'student',
       'user',
       1
WHERE NOT EXISTS (SELECT 1 FROM b_user WHERE username = '2016001');

-- 普通管理员：admin / 123456
INSERT INTO b_user (username, password, salt, real_name, phone, role, status)
SELECT 'admin',
       MD5(CONCAT('123456', 'def123456789012')),
       'def123456789012',
       'admin',
       '18800001111',
       'admin',
       1
WHERE NOT EXISTS (SELECT 1 FROM b_user WHERE username = 'admin');

-- 超级管理员：super / 123456
INSERT INTO b_user (username, password, salt, real_name, phone, role, status)
SELECT 'super',
       MD5(CONCAT('123456', 'ghi123456789012')),
       'ghi123456789012',
       'super',
       '18800002222',
       'super',
       1
WHERE NOT EXISTS (SELECT 1 FROM b_user WHERE username = 'super');
