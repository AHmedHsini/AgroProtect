package com.agriplatform.identity.repository;

import com.agriplatform.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true")
    Set<Role> findSystemRoles();

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    Set<Role> findAllWithPermissions();
}
