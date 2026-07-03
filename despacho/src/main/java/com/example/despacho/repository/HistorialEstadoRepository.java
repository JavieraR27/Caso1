package com.example.despacho.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.despacho.model.HistorialEstado;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    List<HistorialEstado> findBySeguimientoIdOrderByFechaCambioAsc(int seguimientoId);
}
