package ru.privetdruk.restorder.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.model.enums.City;

import java.util.List;

@Repository
public interface TavernRepository extends CrudRepository<TavernEntity, Long> {
    List<TavernEntity> findAllByAddress_CityAndCategory(City city, Category category);
}
