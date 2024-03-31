package jeff.persistent.model.mysql.dao;

import jeff.persistent.model.mysql.po.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GoodsDAO extends JpaRepository<Goods, Integer> {

    /**
     * @param gQuantity 欲下單的商品數量
     * @param gId       欲下單的商品ID
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE `goods` SET stock = stock - :gQuantity WHERE id = :gId", nativeQuery = true)
    void destock(@Param("gQuantity") int gQuantity, @Param("gId") int gId);


}
