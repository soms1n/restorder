package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.City;

import java.util.List;
import java.util.Optional;

@Repository
public interface TavernRepository extends CrudRepository<TavernEntity, Long> {
    List<TavernEntity> findAllByValidAndAddressCityAndCategoryOrderByName(Boolean valid, City city, Category category);

    @EntityGraph(attributePaths = {"employees.roles"})
    Optional<TavernEntity> findById(Long id);

    @EntityGraph(attributePaths = {"address"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithAddress(Long id);

    @EntityGraph(attributePaths = {"contacts"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithContacts(Long id);

    @EntityGraph(attributePaths = {"tables"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithTables(Long id);

    @EntityGraph(attributePaths = {"schedules"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithSchedules(Long id);

    @EntityGraph(attributePaths = {"employees.roles"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithEmployees(Long id);
}
