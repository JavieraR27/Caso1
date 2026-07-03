package com.example.clientes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.clientes.model.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    List<Direccion> findByClienteId(int clienteId);
}
