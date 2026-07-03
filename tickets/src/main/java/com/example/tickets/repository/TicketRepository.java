package com.example.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tickets.model.EstadoTicket;
import com.example.tickets.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByEstado(EstadoTicket estado);

    List<Ticket> findByClienteId(int clienteId);

    List<Ticket> findByEstadoAndClienteId(EstadoTicket estado, int clienteId);
}
