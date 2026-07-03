package com.example.administrador.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.administrador.model.ReporteSemanal;

@Repository
public interface ReporteSemanalRepository extends JpaRepository<ReporteSemanal, Integer> {
}
