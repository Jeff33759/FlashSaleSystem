package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.FlashSaleEvent;
import jeff.persistent.model.mysql.po.SaleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SaleEventDAO extends JpaRepository<SaleEvent, Integer> {

    /**
     * 更改銷售案件的狀態。
     * 上架中:is_public=true，已下架:is_public=false
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE `sale_event` SET `is_public` = ?2 WHERE id = ?1 ", nativeQuery = true)
    void updateStateById(int seId, boolean isPublic);

}
