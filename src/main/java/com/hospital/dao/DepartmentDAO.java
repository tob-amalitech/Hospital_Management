package com.hospital.dao;

import com.hospital.model.Department;

import java.util.List;

public interface DepartmentDAO {
    int create(Department department) throws Exception;
    Department findById(int id) throws Exception;
    List<Department> findAll() throws Exception;
    boolean update(Department department) throws Exception;
    boolean delete(int id) throws Exception;
}
