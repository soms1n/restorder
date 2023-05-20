package ru.privetdruk.restorder.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.TableEntity;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;
import ru.privetdruk.restorder.model.enums.ReserveStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReserveRepository extends CrudRepository<ReserveEntity, Long> {
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status AND reserve.date = :date
            """)
    List<ReserveEntity> findByTavernAndStatus(TavernEntity tavern, LocalDate date, ReserveStatus status);

    @EntityGraph(attributePaths = "table")
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status
            """)
    List<ReserveEntity> findByTavernAndStatusWithTable(TavernEntity tavern, ReserveStatus status);

    @EntityGraph(attributePaths = "table")
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status AND reserve.date = :date
            """)
    List<ReserveEntity> findByTavernAndStatusWithTable(TavernEntity tavern, LocalDate date, ReserveStatus status);

    @EntityGraph(attributePaths = {"table", "user.contacts"})
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status
            """)
    List<ReserveEntity> findByTavernAndStatusWithTableUser(TavernEntity tavern, ReserveStatus status);

    @EntityGraph(attributePaths = {"table.tavern", "user.contacts"})
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status
            """)
    List<ReserveEntity> findByTavernAndStatusWithTableUserTavern(TavernEntity tavern, ReserveStatus status);

    @EntityGraph(attributePaths = {"table", "user"})
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.id = :id AND reserve.status = :status
            """)
    ReserveEntity findByIdAndTavernAndStatusWithTableUserTavern(Long id, TavernEntity tavern, ReserveStatus status);

    @EntityGraph(attributePaths = {"table", "user.contacts"})
    @Query("""
            SELECT reserve
             FROM ReserveEntity reserve
                JOIN TableEntity table ON table = reserve.table
                JOIN TavernEntity tavern ON tavern = table.tavern
             WHERE tavern = :tavern AND reserve.status = :status AND reserve.date = :date
            """)
    List<ReserveEntity> findByTavernAndStatusWithTableUser(TavernEntity tavern, LocalDate date, ReserveStatus status);

    List<ReserveEntity> findByTableAndStatus(TableEntity table, ReserveStatus status);

    List<ReserveEntity> findByTableAndDateAndStatus(TableEntity table, LocalDate date, ReserveStatus status);

    @EntityGraph(attributePaths = "table.tavern")
    List<ReserveEntity> findByUserAndStatus(UserEntity user, ReserveStatus status);
}
