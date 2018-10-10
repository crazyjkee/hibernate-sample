package ru.redsys.sample.hibernate.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.redsys.sample.hibernate.model.Document;

import java.util.Optional;

@Repository
public interface DocumentRepository extends CrudRepository<Document, Integer> {

    boolean existsByName(String name);
}
