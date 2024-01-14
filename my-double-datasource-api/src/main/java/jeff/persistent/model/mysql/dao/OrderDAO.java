package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderDAO extends JpaRepository<Orders, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE `orders` SET status = :status WHERE id = :id", nativeQuery = true)
    void updateStatusById(@Param("id") int id, @Param("status") int status);

}
