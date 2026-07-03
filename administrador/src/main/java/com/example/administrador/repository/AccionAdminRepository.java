package com.example.administrador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.administrador.model.AccionAdmin;

@Repository
public interface AccionAdminRepository extends JpaRepository<AccionAdmin, Integer> {

    List<AccionAdmin> findAllByOrderByFechaDesc();
}
