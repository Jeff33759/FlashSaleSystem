# 更新日誌

## [Unreleased]

## Added
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

## [0.2.1-SNAPSHOT] - 2023-05-13

### Changed
- DB的Schema更動，把快閃銷售案件跟一般銷售案件改成做在同張案件表裡，用type欄位區分案件類型

### Added
- 一般銷售案件的下單API
- core-processor的Logging以及例外的處理

## [0.1.1-SNAPSHOT] - 2023-04-02

### Added
- 父專案的建構，Spring相關版本統一管理，引入maven定版插件
- common-lob子專案的建構，做為各專案共通的函式庫
- 雙數據源Api子專案的建構，數據源分別為Mongo與Mysql
- core-processor子專案的建構，程式啟動後自動完成DEV環境建置(如DB等...)
- redis-api子專案建構