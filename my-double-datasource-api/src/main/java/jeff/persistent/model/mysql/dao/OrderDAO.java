package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDAO extends JpaRepository<Orders, Integer> {


}
