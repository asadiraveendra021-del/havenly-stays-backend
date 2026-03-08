package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.Role;
import com.asadi.havenly_stays.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
