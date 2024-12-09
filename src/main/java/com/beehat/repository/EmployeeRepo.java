package com.beehat.repository;

import com.beehat.entity.Employee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee,Integer> {

    @Query("SELECT e FROM Employee e WHERE e.id = :id1")
    Employee findeById(@Param("id1") Integer id);
    @Transactional
    @Query("SELECT e FROM Employee e WHERE e.username = :username")
    Employee findByUsername(@Param("username") String username);
    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Employee findByEmail(@Param("email") String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    // Phương thức tìm tất cả nhân viên theo trạng thái và vai trò
    @Query("SELECT e FROM Employee e WHERE (:status IS NULL OR e.status = :status) " +
            "AND (:role IS NULL OR e.role = :role)")
    List<Employee> findAllEmployees(@Param("status") Byte statusValue, @Param("role") Byte roleValue);

    // Phương thức tìm nhân viên theo tên, username hoặc số điện thoại
    @Query("SELECT e FROM Employee e WHERE " +
            "(LOWER(e.fullname) LIKE LOWER(CONCAT('%', :searchValue, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :searchValue, '%')) OR " +
            "LOWER(e.phone) LIKE LOWER(CONCAT('%', :searchValue, '%'))) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:role IS NULL OR e.role = :role)")
    List<Employee> findEmployeesBySearchValue(@Param("searchValue") String searchValue,
                                              @Param("status") Byte statusValue,
                                              @Param("role") Byte roleValue);
}
