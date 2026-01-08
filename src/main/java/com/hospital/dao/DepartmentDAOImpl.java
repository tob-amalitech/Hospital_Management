package com.hospital.dao;

import com.hospital.model.Department;
import com.hospital.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for DepartmentDAO
 */
public class DepartmentDAOImpl implements DepartmentDAO {

    @Override
    /**
     * Creates a new department in the database.
     * 
     * @param department The department object containing details to insert.
     * @return The generated department ID, or -1 if creation failed.
     * @throws Exception If a database error occurs.
     */
    public int create(Department department) throws Exception {
        String sql = "INSERT INTO department (department_name, location) VALUES (?,?) RETURNING department_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getLocation());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Inserted department id=" + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating department: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    @Override
    /**
     * Finds a department by its ID.
     * 
     * @param id The ID of the department to find.
     * @return The Department object if found, null otherwise.
     * @throws Exception If a database error occurs.
     */
    public Department findById(int id) throws Exception {
        String sql = "SELECT department_id, department_name, location FROM department WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Department d = new Department();
                    d.setDepartmentId(rs.getInt("department_id"));
                    d.setDepartmentName(rs.getString("department_name"));
                    d.setDescription(null);
                    d.setLocation(rs.getString("location"));
                    return d;
                }
            }
        }
        return null;
    }

    @Override
    /**
     * Retrieves all departments from the database.
     * 
     * @return A list of all Department objects.
     * @throws Exception If a database error occurs.
     */
    public List<Department> findAll() throws Exception {
        String sql = "SELECT department_id, department_name, location FROM department";
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(null);
                d.setLocation(rs.getString("location"));
                list.add(d);
            }
        }
        return list;
    }

    @Override
    /**
     * Updates an existing department's information.
     * 
     * @param department The department object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean update(Department department) throws Exception {
        String sql = "UPDATE department SET department_name=?, location=? WHERE department_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getLocation());
            ps.setInt(3, department.getDepartmentId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    /**
     * Deletes a department by its ID.
     * 
     * @param id The ID of the department to delete.
     * @return true if the deletion was successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM department WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }
}
