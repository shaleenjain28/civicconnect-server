package com.civicconnect.server.repository;

import com.civicconnect.server.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DepartmentRepository — Data Access Layer for Department
 *
 * Extends JpaRepository<Department, Integer> giving us 15+ free DB methods:
 *   findAll(), findById(), save(), deleteById(), count(), existsById() etc.
 *
 * Generic parameters:
 *   Department → the entity this repository manages
 *   Integer    → the primary key type (Department.id is Integer)
 *
 * @Repository:
 *   1. Marks this as a Spring bean — Spring creates an implementation at startup.
 *   2. Enables exception translation — DB errors → Spring DataAccessException hierarchy.
 *
 * Spring Data generates the implementation at runtime via a proxy.
 * You declare WHAT queries you need; Spring figures out HOW to run them.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    /**
     * Find a department by its URL slug.
     * Spring reads "findBySlug" and generates: SELECT * FROM departments WHERE slug = ?
     * No @Query annotation needed — the method name IS the query.
     *
     * Returns Optional<Department>:
     *   - Present → department found
     *   - Empty   → no department with that slug (caller must handle with orElseThrow)
     */
    Optional<Department> findBySlug(String slug);
}
