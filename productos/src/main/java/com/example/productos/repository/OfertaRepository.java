package com.example.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.productos.model.Oferta;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    List<Oferta> findByProductoId(int productoId);

    List<Oferta> findByProductoIdAndActivaTrue(int productoId);
}
