package it.alf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.alf.entity.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

}
