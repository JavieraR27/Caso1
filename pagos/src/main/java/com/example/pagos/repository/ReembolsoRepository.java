package com.example.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pagos.model.Reembolso;

@Repository
public interface ReembolsoRepository extends JpaRepository<Reembolso, Integer> {

    List<Reembolso> findByPagoId(int pagoId);
}
