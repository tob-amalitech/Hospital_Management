-- 1.1 DEPARTMENT
CREATE TABLE department (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(100)
);

-- 1.2 PATIENT
CREATE TABLE patient (
    patient_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    address TEXT,
    blood_group VARCHAR(10),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 1.3 DOCTOR
CREATE TABLE doctor (
    doctor_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    license_number VARCHAR(50) UNIQUE,
    department_id INT NOT NULL,
    CONSTRAINT fk_doctor_department
        FOREIGN KEY (department_id)
        REFERENCES department(department_id)
        ON DELETE RESTRICT
);

-- 1.4 APPOINTMENT
CREATE TABLE appointment (
    appointment_id SERIAL PRIMARY KEY,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Scheduled', 'Completed', 'Cancelled')),
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(patient_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES doctor(doctor_id)
        ON DELETE CASCADE
);

-- 1.5 MEDICAL_RECORD
CREATE TABLE medical_record (
    record_id SERIAL PRIMARY KEY,
    diagnosis TEXT NOT NULL,
    treatment TEXT,
    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    CONSTRAINT fk_record_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(patient_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_record_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES doctor(doctor_id)
        ON DELETE SET NULL
);

-- 1.6 MEDICAL_INVENTORY
CREATE TABLE medical_inventory (
    inventory_id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    item_type VARCHAR(50) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    expiry_date DATE
);

-- 1.7 PRESCRIPTION
CREATE TABLE prescription (
    prescription_id SERIAL PRIMARY KEY,
    prescription_date DATE NOT NULL DEFAULT CURRENT_DATE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT UNIQUE,
    CONSTRAINT fk_prescription_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(patient_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES doctor(doctor_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_prescription_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES appointment(appointment_id)
        ON DELETE SET NULL
);

-- 1.8 PRESCRIPTION_ITEM
CREATE TABLE prescription_item (
    prescription_item_id SERIAL PRIMARY KEY,
    dosage VARCHAR(50) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    prescription_id INT NOT NULL,
    inventory_id INT NOT NULL,
    CONSTRAINT fk_item_prescription
        FOREIGN KEY (prescription_id)
        REFERENCES prescription(prescription_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES medical_inventory(inventory_id)
        ON DELETE RESTRICT
);

-- 1.9 PATIENT_FEEDBACK
CREATE TABLE patient_feedback (
    feedback_id SERIAL PRIMARY KEY,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    feedback_date DATE NOT NULL DEFAULT CURRENT_DATE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    CONSTRAINT fk_feedback_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(patient_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_feedback_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES doctor(doctor_id)
        ON DELETE CASCADE
);

-- 2. Indexing Strategy
CREATE INDEX idx_patient_name ON patient (last_name, first_name);
CREATE INDEX idx_patient_email ON patient (email);
CREATE INDEX idx_appointment_date ON appointment (appointment_date);
CREATE INDEX idx_appointment_doctor ON appointment (doctor_id);
CREATE INDEX idx_doctor_department ON doctor (department_id);
CREATE INDEX idx_prescription_patient ON prescription (patient_id);
CREATE INDEX idx_medical_record_patient ON medical_record (patient_id);
CREATE INDEX idx_inventory_item_name ON medical_inventory (item_name);
