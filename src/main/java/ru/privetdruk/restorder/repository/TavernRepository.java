package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.City;

import java.util.List;

@Repository
public interface TavernRepository extends CrudRepository<TavernEntity, Long> {
    List<TavernEntity> findAllByValidAndAddressCityAndCategoryOrderByName(Boolean valid, City city, Category category);

    @EntityGraph(attributePaths = {"address", "contacts", "schedules", "employees.roles"})
    TavernEntity findByIdAndAddressCity(Long id, City city);

    @EntityGraph(attributePaths = {"schedules", "employees.roles", "tables.reserves"})
    @Query("SELECT t FROM TavernEntity t WHERE t.id = :id")
    TavernEntity findByIdWithSchedulesAndReserves(Long id);
}
