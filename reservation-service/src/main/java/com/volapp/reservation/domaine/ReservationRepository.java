package com.volapp.reservation.domaine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Acces aux reservations (base PostgreSQL "reservations"). */
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
