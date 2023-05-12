package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.OrdersDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailDAO extends JpaRepository<OrdersDetail, Integer> {

}
