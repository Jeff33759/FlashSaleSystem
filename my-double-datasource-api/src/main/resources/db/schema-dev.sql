-- 用於develop環境的DB建置檔 --

CREATE DATABASE IF NOT EXISTS db_dev_flash_sale_module DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE db_dev_flash_sale_module;

CREATE TABLE IF NOT EXISTS `member`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `role` INT(1) DEFAULT 1 COMMENT '1:一般會員(只能下單)，2:企業主(只能發單)',
    `status` INT(1) DEFAULT 1 COMMENT '1:啟用狀態，2:黑名單狀態，3:軟刪除狀態(凍結)',
    `create_time` TIMESTAMP NOT NULL,
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='會員表';

CREATE TABLE IF NOT EXISTS `goods`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `m_id` INT UNSIGNED COMMENT '新增此商品的會員ID(其會員身分必為企業主)',
    `name` VARCHAR(100) NOT NULL,
    `storage` INT UNSIGNED DEFAULT 0 COMMENT '庫存數量，設定只為正數',
    `price` INT NOT NULL,
    FOREIGN KEY(`m_id`) REFERENCES `member`(id),
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `sale_event`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `g_id` INT UNSIGNED COMMENT '此銷售案件所販賣的商品ID',
    `status` BOOLEAN DEFAULT true COMMENT 't: 上架中，f:下架中',
    `start_time` TIMESTAMP NOT NULL COMMENT '上架時間',
    `end_time` TIMESTAMP NOT NULL COMMENT '預計下架時間',
    FOREIGN KEY(`g_id`) REFERENCES `goods`(id),
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='一般銷售案件';

CREATE TABLE IF NOT EXISTS `flash_sale_event`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `g_id` INT UNSIGNED COMMENT '此銷售案件所販賣的商品ID',
    `status` BOOLEAN DEFAULT true COMMENT 't: 上架中，f:下架中',
    `start_time` TIMESTAMP NOT NULL COMMENT '上架時間',
    `end_time` TIMESTAMP NOT NULL COMMENT '預計下架時間',
    FOREIGN KEY(`g_id`) REFERENCES `goods`(id),
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限時搶購案件';

CREATE TABLE IF NOT EXISTS `order`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `fs_id` INT UNSIGNED COMMENT '對應的限時搶購案件編號(如果有的話)',
    `m_id` INT UNSIGNED COMMENT '下單的會員ID(其會員身分必為一般會員)',
    `total` INT DEFAULT 0 COMMENT '訂單總金額',
    `status` INT(1) DEFAULT 1 COMMENT '訂單狀態。1:進行中，2:已完成，3:已取消',
    `create_time` TIMESTAMP NOT NULL COMMENT '訂單創建時間',
    `fstr_id` VARCHAR(100) COMMENT 'flash_sale_temp_record搶購臨時表的對應id(如果有的話)，來自MongoDB',
    FOREIGN KEY(`fs_id`) REFERENCES `flash_sale_event`(id),
    FOREIGN KEY(`m_id`) REFERENCES `member`(id),
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='訂單表';


CREATE TABLE IF NOT EXISTS `order_detail`(
    `id` INT UNSIGNED AUTO_INCREMENT,
    `o_id` INT UNSIGNED COMMENT '對應的訂單ID',
    `g_id` INT UNSIGNED COMMENT '對應的商品ID',
    `quantity` INT UNSIGNED COMMENT '下訂的數量',
    FOREIGN KEY(`o_id`) REFERENCES `order`(id),
    FOREIGN KEY(`g_id`) REFERENCES `goods`(id),
    PRIMARY KEY ( `id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='訂單明細表';
