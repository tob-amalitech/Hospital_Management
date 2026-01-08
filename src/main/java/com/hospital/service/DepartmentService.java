package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.dao.DepartmentDAOImpl;
import com.hospital.model.Department;
import com.hospital.util.Cache;
import com.hospital.util.ValidationUtil;

import java.util.List;

public class DepartmentService {
    private final DepartmentDAO dao = new DepartmentDAOImpl();
    private final Cache<Integer, Department> cache = new Cache<>();

    public int createDepartment(Department d) throws Exception {
        if (!ValidationUtil.validateRequired(d.getDepartmentName())) throw new IllegalArgumentException("Department name required");
        int id = dao.create(d);
        if (id > 0) { d.setDepartmentId(id); cache.put(id, d); }
        return id;
    }

    public Department getDepartment(int id) throws Exception { Department d = cache.get(id); if (d!=null) return d; d = dao.findById(id); if (d!=null) cache.put(id,d); return d; }
    public List<Department> getAll() throws Exception { return dao.findAll(); }
    public boolean updateDepartment(Department d) throws Exception { boolean ok = dao.update(d); if (ok) cache.put(d.getDepartmentId(), d); return ok; }
    public boolean deleteDepartment(int id) throws Exception { return dao.delete(id); }
}
