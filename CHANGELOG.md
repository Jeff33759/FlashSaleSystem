# 更新日誌

## [Unreleased]

### Changed
- DB的Schema更動，把快閃銷售案件跟一般銷售案件改成做在同張案件表裡，用type欄位區分案件類型

## [0.1.1-SNAPSHOT] - 2023-04-02

### Added
- 父專案的建構，Spring相關版本統一管理，引入maven定版插件
- common-lob子專案的建構，做為各專案共通的函式庫
- 雙數據源Api子專案的建構，數據源分別為Mongo與Mysql
- core-processor子專案的建構，程式啟動後自動完成DEV環境建置(如DB等...)
- redis-api子專案建構