# 更新日誌

## [Unreleased]

## Added
- 更改一般銷售案件狀態(上架中/已下架)的接口完成
- 更改銷售案件狀態為已下架的接口完成
- 新增排程: 快閃銷售案件如果到達end_time，代表已經過了設定的販售時間，走下架流程

## Changed
- 修改排程: 快閃銷售案件依照start_time決定開賣時間，還沒到點的不會開賣

## Fixed
- 修正core-processor當接口為Get類型時，因為沒有body而在WrapperFilter噴錯

## [0.4.1] - 2024-02-28

### Added
- core-processor新增完成訂單的接口
- high-concurrency-processor完成過濾鏈撰寫
- my-mq-lib新增，並建置各系統關於MQ的發布/消費架構
- high-concurrency-processor完成aop例外捕捉
- 快閃銷售案件的接口與整個流程功能完成

## [0.3.1] - 2024-01-14

### Added
- 新增子專案high-concurrency-processor
- 新增子專案scheduler-processor
- 新增排程:掃描快閃銷售案件進mongo
- 系統啟動時清除mongo、redis所有資料
- 新增子專案my-test-lib，這一包裡面放些專門用於測試的東西，如POJO、自寫工具、第三方函式庫等等。
- core-processor新增init的接口，方便DEMO
- high-concurrency-processor的reactive相關元件框架完成

### Changed
- DB的Schema更動，把快閃銷售案件跟一般銷售案件又拆回兩張表了。
- 統一Log格式(為了將來日誌中心化)、新增Log工具包
- redis-api重構，並把噴錯統一成MyRedisException

## [0.2.1] - 2023-05-13

### Changed
- DB的Schema更動，把快閃銷售案件跟一般銷售案件改成做在同張案件表裡，用type欄位區分案件類型

### Added
- 一般銷售案件的下單API
- core-processor的Logging以及例外的處理

## [0.1.1] - 2023-04-02

### Added
- 父專案的建構，Spring相關版本統一管理，引入maven定版插件
- common-lob子專案的建構，做為各專案共通的函式庫
- 雙數據源Api子專案的建構，數據源分別為Mongo與Mysql
- core-processor子專案的建構，程式啟動後自動完成DEV環境建置(如DB等...)
- redis-api子專案建構