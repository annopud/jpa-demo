DROP TABLE IF EXISTS general_testing;

CREATE TABLE general_testing (
  id VARCHAR(32) NOT NULL COMMENT 'Case ID',
  type INT NOT NULL COMMENT 'Case type',
  email_from VARCHAR(255) NOT NULL COMMENT 'Notification email from addresses',
  email_to VARCHAR(255) NOT NULL COMMENT 'Notification email to addresses',
  email_cc VARCHAR(255) DEFAULT NULL COMMENT 'Notification email cc addresses',
  email_bcc VARCHAR(255) DEFAULT NULL COMMENT 'Notification email bcc addresses',
  email_submit_param LONGBLOB DEFAULT NULL COMMENT 'Submission param as JSON',
  email_error TEXT DEFAULT NULL COMMENT 'Notification email error detail',
  email_status ENUM('S','E') DEFAULT NULL COMMENT 'Notification email send status (S=Success; E=Error)',
  email_date DATETIME DEFAULT NULL COMMENT 'Notification email send date',
  create_by VARCHAR(6) NOT NULL,
  create_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(6) DEFAULT NULL,
  update_date DATETIME DEFAULT NULL,
  PRIMARY KEY (id)
) COMMENT='Table for general testing purposes';

INSERT INTO general_testing (id,`type`,email_from,email_to,email_cc,email_bcc,email_status,create_date,create_by,update_date)
	VALUES ('CASE1234567890123',1,'sender@example.com','recipient@example.com','cc@example.com','bcc@example.com','S',CURRENT_TIMESTAMP,'00002',CURRENT_TIMESTAMP);
