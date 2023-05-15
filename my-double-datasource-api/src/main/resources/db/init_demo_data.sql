-- 建置DEMO用資料 --
USE db_dev_flash_sale_module;

-- 以防萬一，先把所有表都Truncate了 --
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `goods`;
TRUNCATE TABLE `members`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `orders_detail`;
TRUNCATE TABLE `sale_event`;
SET
FOREIGN_KEY_CHECKS = 1;

INSERT INTO members
VALUES (NULL, 'Jeff', 1, 1, '2022-09-19 20:57:43'),
       (NULL, 'Amanda Company', 2, 1, '2023-01-10 20:00:00'),
       (NULL, 'omega', 1, 2, CURRENT_TIMESTAMP());

INSERT INTO goods
VALUES (NULL, 2, '螺絲套組', 500, 200),
       (NULL, 2, '板手套組', 200, 1000),
       (NULL, 2, '限量套組', 50, 2000);

INSERT INTO `sale_event`
VALUES (NULL, 1, DEFAULT, '2023-01-12 19:00:00'),
       (NULL, 2, DEFAULT, '2023-02-12 19:00:00');

INSERT INTO `flash_sale_event`
VALUES (NULL, 3, DEFAULT, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP() + INTERVAL 5 MINUTE, DEFAULT);