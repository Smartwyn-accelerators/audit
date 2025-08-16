

-- We can create a demo application for a Timesheet Management System that cover the following modules use cases:

-- 1. Authentication/Authorization/SSO: To add login.
-- 2. Logging: To log application events in file.
-- 3. Centralized Exception Handling: To demonstrate how Exception Handler handles different types of exceptions globally like EntityNotFoundException and the use of ApiError class in formatting error responses.
-- 4. Email Template Builder: For sending notifications (e.g., email notifications when a timesheet/project/task is created or reminder emails for pending timesheets).
-- 5. User Audit (API History): To track user actions across the system.
-- 6. Entity Audit (Entity History): To monitor changes to entities (e.g., timesheets, users).
-- 7. Document Management API: For handling file uploads (e.g., attaching documents to timesheets).
-- 8. Integration Connectors (REST Connector): To integrate with external services (e.g., integrate fake API such as restcountries to fetch list of countries or jsonplaceholder to fetch list of dummy users).
-- 9. Jobs Scheduler: For scheduling tasks (e.g., reminder emails for pending timesheets).
-- 10. Notifications API: To notify users about important events/announcements using Email or SMS




-- Customer Table
CREATE TABLE customer (
    customerid BIGINT NOT NULL PRIMARY KEY,
    emailaddress VARCHAR(255) NOT NULL,
    payment_details VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    country VARCHAR(255),
    FOREIGN KEY (customerid) REFERENCES customer(customerid) ON DELETE CASCADE
);

-- Project Table
CREATE TABLE project (
    id BIGINT NOT NULL PRIMARY KEY,
    customerid BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    startdate DATE NOT NULL,
    enddate DATE NOT NULL,
    FOREIGN KEY (customerid) REFERENCES customer(customerid) ON DELETE CASCADE
);

-- Task Table
CREATE TABLE task (
    id BIGINT NOT NULL PRIMARY KEY,
    projectid BIGINT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    isactive BOOLEAN NOT NULL,
    FOREIGN KEY (projectid) REFERENCES project(id) ON DELETE SET NULL
);

-- Timeofftype Table
CREATE TABLE timeofftype (
    id BIGINT NOT NULL PRIMARY KEY,
    typename VARCHAR(255) NOT NULL
);

-- Timesheet Table
CREATE TABLE timesheet (
    id BIGINT NOT NULL PRIMARY KEY,
    customerid BIGINT NOT NULL,
    timesheetstatusid BIGINT NOT NULL,
    periodendingdate DATE NOT NULL,
    notes VARCHAR(255),
    periodstartingdate DATE NOT NULL,
    FOREIGN KEY (customerid) REFERENCES customer(customerid) ON DELETE CASCADE
);

-- Timesheetdetails Table
CREATE TABLE timesheetdetails (
    id BIGINT NOT NULL PRIMARY KEY,
    taskid BIGINT,
    timesheetid BIGINT NOT NULL,
    timeofftypeid BIGINT,
    workdate DATE NOT NULL,
    hours NUMERIC,
    notes VARCHAR(255),
    attachments TEXT[],
    FOREIGN KEY (taskid) REFERENCES task(id) ON DELETE SET NULL,
    FOREIGN KEY (timesheetid) REFERENCES timesheet(id) ON DELETE CASCADE,
    FOREIGN KEY (timeofftypeid) REFERENCES timeofftype(id) ON DELETE SET NULL
);

-- Timesheetstatus Table
CREATE TABLE timesheetstatus (
    id BIGINT NOT NULL PRIMARY KEY,
    statusname VARCHAR(255) NOT NULL
);

-- Usertask Table
CREATE TABLE usertask (
    customerid BIGINT NOT NULL,
    taskid BIGINT NOT NULL,
    PRIMARY KEY (customerid, taskid),
    FOREIGN KEY (customerid) REFERENCES customer(customerid) ON DELETE CASCADE,
    FOREIGN KEY (taskid) REFERENCES task(id) ON DELETE CASCADE
);

