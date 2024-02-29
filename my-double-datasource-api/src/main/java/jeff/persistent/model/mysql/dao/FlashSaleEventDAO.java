package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.FlashSaleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FlashSaleEventDAO extends JpaRepository<FlashSaleEvent, Integer> {

    /**
     * 得到上架中並且還沒被排程掃描過的快閃銷售活動
     */
    @Query(value = "SELECT * FROM `flash_sale_event` WHERE is_public = true AND has_been_scanned = false ", nativeQuery = true)
    List<FlashSaleEvent> selectFlashSaleEventWhichIsPublicAndHasNotBeenScanned();

    /**
     * 更改銷售案件的狀態。
     * 上架中:is_public=true，已下架:is_public=false
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE `flash_sale_event` SET `is_public` = ?2 WHERE id = ?1 ", nativeQuery = true)
    void updateStateById(int fseId, boolean isPublic);

}
