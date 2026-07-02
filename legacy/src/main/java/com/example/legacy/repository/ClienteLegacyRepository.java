package com.example.legacy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.legacy.model.ClienteLegacy;

@Repository
public interface ClienteLegacyRepository extends JpaRepository<ClienteLegacy, Integer> {

    Optional<ClienteLegacy> findByEmail(String email);

    boolean existsByEmail(String email);
}
