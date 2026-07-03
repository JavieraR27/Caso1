package com.example.ventas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ventas.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findByClienteId(int clienteId);

    List<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    /** Órdenes que incluyen productos de un proveedor (join a los detalles). */
    @Query("SELECT DISTINCT v FROM Venta v JOIN v.detalles d WHERE d.proveedorId = :proveedorId")
    List<Venta> findByProveedorId(@Param("proveedorId") int proveedorId);
}
