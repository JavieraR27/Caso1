package com.example.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tickets.model.TicketMensaje;

@Repository
public interface TicketMensajeRepository extends JpaRepository<TicketMensaje, Integer> {

    List<TicketMensaje> findByTicketIdOrderByFechaAsc(int ticketId);
}
