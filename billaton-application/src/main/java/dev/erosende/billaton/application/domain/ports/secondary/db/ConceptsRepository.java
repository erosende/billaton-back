package dev.erosende.billaton.application.domain.ports.secondary.db;

import dev.erosende.billaton.application.domain.model.ConceptDto;

import java.util.List;

public interface ConceptsRepository {

  List<ConceptDto> findConcepts(Integer documentId);

  Integer saveConcept(ConceptDto concept);

  void updateConcept(ConceptDto concept);

  void deleteConcept(Integer conceptId, Integer documentId);

  void deleteByDocumentId(Integer documentId);

}
