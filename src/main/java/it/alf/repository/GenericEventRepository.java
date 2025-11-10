package it.alf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.alf.entity.GenericEventEntity;

@Repository
public interface GenericEventRepository extends JpaRepository<GenericEventEntity, String> {

}
