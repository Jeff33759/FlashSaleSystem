# 更新日誌

## [Unreleased]

## Added
- high-concurrency-processor導入reactive-feign(響應式Http客戶端封裝)，結合consul，實現調用其他微服務時的客戶端軟負載均衡。
- 實作跨服務的UUID。只要是同個業務場景的鏈路，都使用同個UUID，方便之後日誌集中化，可以用同一組UUID抓出跨服務的整條業務鏈路日誌。(之後可以考慮改成Spring Cloud Sleuth)

## Changed
- 微服務之間溝通，改成不再固定狀態碼200，而是根據情況有不同的Http Status。此更動是為了方便之後跟resilience4j整合。

### Fixed
- 所有Consul客戶端註冊進服務中心時，實例的訪問位址原本是預設的主機名，現改成IP。

## [0.7.1] - 2024-03-08

### Added
- core-processor、high-concurrency-processor導入服務註冊/發現(使用Consul)
- 所有伺服端，皆導入"根據mvn package -P 參數，打包出不同環境所需jar"的功能
- core-processor、high-concurrency-processor在LoggingFilter新增忽略邏輯，某些Api不logging。

## [0.6.1] - 2024-03-06

### Added
- core-processor新增查詢快閃銷售案件資料的接口(最終用於前端渲染)
- 導入resilience4j(提升服務容錯率的方案)
- core-processor查詢快閃銷售案件資料的接口-使用限流器限流
- core-processor新增訂單的接口-使用斷路器保護
- high-concurrency-processor的消費快閃銷售案件的接口-使用限流器限流

## [0.5.1] - 2024-03-01

### Added
- 更改一般銷售案件狀態(上架中/已下架)的接口完成
- 更改銷售案件狀態為已下架的接口完成
- 新增排程: 快閃銷售案件如果到達end_time，代表已經過了設定的販售時間，走下架流程

### Changed
- 修改排程: 快閃銷售案件依照start_time決定開賣時間，還沒到點的不會開賣

### Fixed
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