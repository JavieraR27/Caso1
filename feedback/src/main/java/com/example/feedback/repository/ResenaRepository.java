package com.example.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.feedback.model.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    List<Resena> findByProductoId(int productoId);

    List<Resena> findByClienteId(int clienteId);

    boolean existsByClienteIdAndProductoIdAndVentaId(int clienteId, int productoId, int ventaId);
}
