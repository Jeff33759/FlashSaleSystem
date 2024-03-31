# My-flash-sale-system
秒殺級電商系統

<br><br>
## 專案大綱

前陣子日本超人氣歌手LiSA宣布來台舉辦演唱會，門票在一分鐘內被一掃而空，雖不到"秒"殺級別，但高併發的場景往往會對系統與DB造成極大負擔。

本專題不針對DB端做任何設置，志在先從應用程式端的角度切入，利用各種解決方案學習應付高併發的業務需求，實作一個分散式、高擴充性、高容錯率、非阻塞、好除錯的後端電商系統。

<br>

本專題接觸到的技術與概念：
1. Spring Boot(2.7.2)、Spring Data(2.7.2)、WebFlux(2.7.2)、Spring Cloud(2021.0.5)
   + 選用Spring Boot 2.7開發，此版本官方說最方便移植到Spring Boot 3，是2和3的銜接版本。
2. Reactive programming、NIO(non-blocking IO)
3. Message queue、In-Memory DB
4. 服務治理(服務註冊、服務發現)、負載均衡(Load balance)
5. 服務降級、服務熔斷、服務限流、服務雪崩
6. 網關(gateway)
7. Elastic stack-日誌集中化、分散式日誌追蹤
8. Maven專案管理-函式庫版本控管、打包

<br><br>
## 我做到了什麼

### 1. 分散式系統架構

+ 多個實例可以用同個jar去啟動，但啟動命令要帶以下參數:
    + 「-Dapp.instance.hostname」：實例對外的訪問位址。
    + 「-Dserver.port」：實例跑在機器的哪一個port。
    + 「-Dapp.instance.name」：本實例的名稱。


### 2. Maven專案管理
+ 把模組的共同方法抽出，封裝成共用函式庫，再用Maven去注入，統一控制板號。
+ 結合spring-cloud-starter-bootstrap函式庫，讓同一包Source code可以打包(package)成各個環境都可以正常運行的執行檔(jar)。
    + 利用mvn clean package -P <profile-id>指令，在打包時指定bootstrap.yml的profile，打包出來的jar在啟動時，便會根據profile再去讀取不同的application.yml。 
    + 例如同一包Source code想要發布到dev環境與prod環境，這兩個環境的DB連線位址不同，這時候只要先寫好application-dev.yml和application.prod.yml，利用mvn指令打包時，就可以藉由參數打包出兩種jar，一種跑在dev環境，一種跑在prod環境。



### 3. 併發量大的服務，採用WebFlux非同步模型去提高伺服器吞吐量。
+ Web container使用WebFlux預設的Netty，支援NIO(non-blocking IO)。



### 4. Reactive程式撰寫 & 基於StepVerifier的Reactive單元測試。
+ 一些跟阻塞有關的操作(如DB讀寫)皆引入Reactive函式庫，實現NIO的讀寫操作。
+ Reactive單元測試使用函式庫:
    + io.projectreactor: reactor-test



### 5. 配置多個DataSource。
+ MySQL用於資料持久化。
+ MongoDB用於暫存一些不適合放進redis的臨時資料，或者格式容易有變動的資料。
+ 使用函式庫為:
    + spring-boot-starter-data-jpa
    + spring-boot-starter-data-mongodb
    + spring-boot-starter-data-mongodb-reactive



### 6. 程式啟動後，自動根據環境去執行所需的SQL腳本，所以不用再手動執行任何SQL去建置環境，只要OS上有安裝對應版本的DB就行。
+ SQL腳本執行完後，應用程式會在Runtime去更改DataSource的URI(參考DoubleDataSourceApi > DataSourceRunner)



### 7. Redis作為中間件，幫助應付高併發的秒殺級搶購活動。
+ 把一些熱點資料快取進redis，避免對DB的頻繁存取。
+ 利用redis-list的lpop&rpush去做一個原子性又可以在各服務間共享的Queue，此Queue就拿來應付秒殺級搶購的業務，每一個搶購請求就是lpop一筆資料，當redis-list已經沒資料了，就代表東西被搶完了。
    + 基於Redis的增刪查改都是單執行緒在執行的特性，即使不用分布式悲觀鎖(會阻塞)或者是watch + multi 指令，在高併發場景下瞬間對redis做大量存取也不會有超賣的問題發生(亦即庫存為0了還可以被下單)，性能與數據一致性都有兼顧到。
+ 使用函式庫為:
    + spring-boot-starter-data-redis
    + spring-boot-starter-data-redis-reactive



### 8. MQ做為流量削峰及各微服務間的廣播、解耦方案。
+ 秒殺級搶購活動一開賣，瞬間湧入大量的搶購請求，本專題的"消化搶購請求"以及"實際成立訂單的流程(有訪問DB)"被拆到了兩個Server。
    + 消化搶購請求的Server接到請求後，會先回給客戶端一些東西，並發message給成立訂單的Server，藉此削減秒殺級搶購活動的"成立訂單流程"的峰值，不要一瞬間大量的搶購請求都直接打進DB，DB可能承受不了那一瞬間。
+ 當MQ用在非廣播的場景時，設置channel group，防止同一則message被不同實例重複消費。
+ 使用函式庫為:
    + spring-cloud-starter-stream-rabbit
        + springCloudStream針對MQ的進行了抽象封裝，方便之後去抽換不同的底層MQ。本專題的版本似乎只支援rabbitMQ和Kafka，選用rabbitMQ。



### 9. Consul作為服務治理中心，配合actuator函式庫作為服務健康檢查方案，實現微服務集群控管。
+ 使用函式庫為:
    + spring-cloud-starter-consul-discovery
    + spring-boot-starter-actuator



### 10. 使用Playtika開發的ReactiveFeign作為HttpClient去實現NIO的HTTP調用，並結合Consul、spring-cloud-loadbalancer實現基於客戶端的軟負載均衡。
+ Spring Cloud Open Feign是阻塞模型，不適合用在WebFlux上。Spring官方推薦使用Playtika開發的ReactiveFeign作為替代方案。
+ 引入Feign套件的目的，是為了簡化HttpClient的重複封裝開發流程，也方便與resilience4j(服務容錯)、consul(服務治理)整合。
+ 微服務的實例只要一啟動，就會向服務治理中心註冊自己，服務治理中心就會把該實例的訪問地址更新進服務列表，然後再把服務列表廣播給底下其他同樣有註冊的服務實例。當其他服務實例互相呼叫時，如果有使用Spring Cloud Loadbalancer，那就可以實現客戶端軟負載均衡，會在呼叫其他服務時，從服務列表記載的各實例的訪問地址中，依照某種策略(預設輪詢)去決定我這個請求要發到該服務的哪個實例。
+ 使用函式庫為:
    + feign-reactor-spring-cloud-starter
    + 官方文件: https://github.com/PlaytikaOSS/feign-reactive/tree/develop


### 11. 針對一些高併發的業務鏈路設置降級、熔斷、限流機制，避免服務雪崩，保障系統容錯率。
+ Spring官方推薦Resilience4j做為系統容錯方案，使用到的組件為服務熔斷用的「CircuitBreaker」，以及服務限流用的「RateLimiter」。
+ 服務雪崩:
    + 某些高併發的場景，會有短時間大量請求打到上游Server，上游Server又請求到下游Server，這時如果下游Server一時之間阻塞了而沒回應，會造成上游Server一直等待也跟著塞住(前提是上游Server的HttpClient沒設超時)，上游Server之上可能又有個上上游Server，這樣問題會越滾越大，此微服務雪崩，何況下游Server在阻塞的同時也會有其他請求接二連三打進去，惡性循環。
+ 服務降級:    
    + 上游Server依賴於下游Server，當下游Server某些開給上游Server的Api阻塞了(可能做到一半某個I/O塞住了)，這時馬上回給上游Server降級後的服務(fallback)，讓上游Server不要因此卡住，此為服務降級。     
+ 服務熔斷:
    + 設定下游Server的某Api如果fallback佔比滿足調用數的一定百分比，代表該Api根本已經無法提供服務了，那接下來的一個時間段內就熔斷該Api，變成只要一接到請求就直接fallback，完全不跑正式邏輯，此為服務熔斷。 
+ 服務限流:
    + 設定下游Server的某api，限制其一秒內只能接幾個請求，超過就fallback，此為服務限流。
+ 系統容錯機制的設計思路:
    + 上游Server的容錯閥值，要設定得比下游Server的容錯閥值還高，也就是上游Server更不容易觸發容錯保護機制。
        + 打個比方，同一條API鏈路，上游Server呼叫下游Server，假設下游Server有設置斷路器，上游Server也有設置斷路器，這時上游Server的斷路器要設置得比下游Server的斷路器更難觸發，因為有時候下游Server的實例1觸發斷路了，但可能實例2還是可以正常服務的，這時候上游Server就不該斷路。
    + 上游Server的容錯保護關閉門檻，要設定得比下游Server的容錯保護關閉門檻還低，也就是上游Server即使觸發了容錯保護機制，也要恢復得比下游Server還要快。
        + 打個比方，同一條API鏈路，上游Server呼叫下游Server，假設下游Server有設置斷路器，上游Server也有設置斷路器，這時上游Server的斷路器從OPEN轉為HALF_OPEN的等待時間，要設置得比下游Server的等待時間還要短。如果不這樣做，就會造成...下遊Server搶先一步進入了HALF_OPEN，既然進入了HALF_OPEN，就意味著下遊Server現在很有可能已經可以提供服務了，但卻因為上游Server還在OPEN而收不到請求，會有效能冗餘(?)的情況。
        + 效能冗餘究竟是好還是不好，待解惑，也許冗餘換來的系統穩定度提昇會更加重要也說不定。
+ 使用的函式庫為:
    + spring-cloud-starter-circuitbreaker-resilience4j
    + spring-cloud-starter-circuitbreaker-reactor-resilience4j
    + 官方配置文件(本專題使用版本1.7.0): https://resilience4j.readme.io/v1.7.0/docs/getting-started



### 12. Gateway做為最上游Server對API進行路由，實現反向代理、負載均衡、流量控制、服務熔斷等等。
+ Spring Cloud Gateway整合resilience4j與Consul，研究Spring Cloud Gateway本身的過濾鏈機制與Handler，以Reactive的方式客製化自己的API路由邏輯以及系統容錯設計。
+ Gateway的容錯機制皆採用CircuitBreaker元件，當接到下游Server非預期的回應又或者根本沒有回應，超過一定占比就在Gateway熔斷掉該業務的API鏈路。
+ 使用函式庫為:
    + spring-cloud-starter-gateway
    + spring-cloud-starter-circuitbreaker-reactor-resilience4j
    + spring-cloud-starter-consul-discovery



### 13. Log利用UUID，實現分散式日誌追蹤。
+ 自製過濾鏈，製作一個生命週期為Request級別的Context物件。Context物件會攜帶一些資訊(如UUID)，貫穿整個請求的業務流程。
    + 例如一個請求進來，在過濾鏈中生成Context，由Context攜帶UUID，這個UUID會被帶到之後的業務流程，每當在任何一個地方logging時，都會記下這個UUID。
+ 只要是同個業務場景的鏈路都會使用同個UUID，包括跨服務的情況也是。
    + 例如上游Server發請求到下游Server，這時兩個Server的日誌都會是同個UUID，方便日誌集中化時，可以用同一組UUID抓出跨服務的整條業務鏈路日誌，實現分散式日誌追蹤。(之後可以考慮改成Spring Cloud Sleuth)



### 14. log4j2整合Elastic stack，統一各服務的Log格式，實現日誌中心化
+ Spring預設log實作為logback，替換成效能更佳的log4j2，版本為2.17.2(好像2.14版又有重大漏洞，小心別用)
+ log4j2.xml配置滾動(rollover)策略，防止log文件無限膨脹，佔據硬碟空間。
+ File Beats客戶端蒐集日誌，撰寫配置檔drop掉不需要的欄位以減少傳輸量，並針對java error stack log的狀況，撰寫正則表示式。
+ Logstash數據處理，配置過濾鏈把純文字的log本體給結構化，拆成幾個Field，優化Elasticsearch查詢。
+ Elasticsearch存放與搜尋日誌，利用Index Lifecycle Management Polocy管理索引的生命週期，結合Index template，實現滾動(rollover)索引，避免ES上的log document無限膨脹，佔據硬碟空間。
+ Kibana視覺化呈現Log搜尋結果
+ Elastic stack相關的個人配置與佈署步驟，參考「my-documents > centralized-log-collection」

<br><br>
## 環境需求(各Server都一樣)

### dev環境(本地)

1. java版本 : 8
2. MongoDB
   + 建議版本5.0.5
   + port使用官方預設的27017
3. MySQL
   + 版本8.0.30(一定要這版)
   + port使用官方預設的3306
   + 帳密設置: root / sasa
     + 如有不同，去「my-double-datasource-api > resources > myDataSource-dev.properties」改
4. rabbitMQ
    + 版本3.12.12
    + 運行於Erlang版本26.2.2
    + port使用官方預設的5672
    + WebUI的port也使用官方預設的15672
    + 帳密設置: guest / guest
        + 如有不同，去「my-mq-lob > resources > myMQ-dev.properties」改
5. consul
    + 建議版本1.13.3
    + port使用官方預設的8500
6. redis
    + 建議版本5.0.14.1
    + port使用官方預設的6379
7. elastic stack
    + 建議版本7.17.18
    + 詳細安裝啟動配置，參考「my-documents > centralized-log-collection」
8. maven(如有需要對Source code打包)
    + 建議版本3.6.3
9. IDE
    + 有用lombok函式庫，所以若要用IDE看Source code，IDE要安裝lombok插件，不然會解析異常，一堆紅色蚯蚓

    
<br><br>
## 如何打包

### [打包條件]
1. 安裝maven建議版本3.6.3以上

### [指令範例]

#### 欲執行於dev環境(本地)
+ mvn clean package -P dev

本專題v1.0.x尚沒有配置其他環境，如要配置，除了各應用要寫對應的yml以外，也要去pom裡面新增profile標籤


## 如何啟動系統

### [啟動條件]
1. 確保MongoDB、MySQL、Consul、Redis、RabbitMQ運行中。
2. Elastic stack自選，要用就啟動。
3. 確保執行環境為JRE-8。

### [java應用啟動建議順序與指令範例]
1. 啟動system-gateway
    + java -jar -Dserver.port=8030 -Dapp.instance.name=Gateway01 -Dapp.instance.hostname=127.0.0.1 system-gateway-1.0.0.jar
2. 啟動high-concurrency-processor
    + java -jar -Dserver.port=8010 -Dapp.instance.name=Concurrency01 -Dapp.instance.hostname=127.0.0.1 high-concurrency-processor-1.0.0.jar
3. 啟動schedule-processor
    + java -jar -Dserver.port=8020 -Dapp.instance.name=Scheduler01 -Dapp.instance.hostname=127.0.0.1 schedule-processor-1.0.0.jar
4. 啟動core-processor
    + java -jar -Dserver.port=8001 -Dapp.instance.name=Core01 -Dapp.instance.hostname=127.0.0.1 core-processor-1.0.0.jar

    
<br><br>
## 系統架構示意圖

![image](https://github.com/Jeff33759/FlashSaleSystem/blob/develop/my-documents/system-introduction/System_Architecture_Diagram.jpg)

<br><br>
## 各java微服務職責大致說明

### [system-gateway]
1. 反向代理
2. 對API進行路由
3. 流量控管、服務熔斷

### [high-currency-processor]
1. 所有高併發的業務請求都由這個服務承接，例如"搶購LiSA門票"等等...... 因此這個服務和gateway一樣，都使用NIO的WebFlux開發。
2. 因為是處理高併發業務，所以不會直接存取MySQL，但會存取MongoDB和Redis。
3. 通常是Message provider。

### [core-processor]
1. 實例啟動後，執行SQL腳本，自動化建置DB、新增DEMO資料
2. 其他非高併發的業務，都由這個服務來做，例如"發布銷售案件"、"成立訂單"等等......其實可以再根據需求拆成更細的微服務，但懶得拆了，一切雜事都由這個服務來做。
3. 會直接存取MySQL，所以有些需要操作MySQL的業務場景，high-currency-processor會發HTTP請求來向core-processor要資料。
4. 通常是Message consumer，所以不管上游Server的併發量多大，到這裡的時候已經被削峰過。 

### [schedule-processor]
1. 專門跑排程的應用程式(整理報表資料等等...)
2. 使用排程預先把熱點資料快取進redis，以避免類似快取擊穿的問題。
    + 例如LiSA的門票銷售活動，預計中午12:00開賣，但是在那之前就會先開放銷售頁面讓會員進入了。渲染銷售頁面的資料來源是DB裡的商品資訊(Query)。如果沒有預先把DB的商品資訊快取進redis，很可能會變成...鄰近12:00前，開放銷售頁面的瞬間(只是開放頁面，但還沒開放搶購)，大家開始瘋狂F5，湧入了大量請求query請求，那個瞬間因為redis還沒有快取，所以通通會去query DB，如果DB承受住了，那麼查詢成功的資料快取進redis，之後的請求都會存取redis，那就沒問題，但萬一DB沒承受住那一瞬間，那就會出事。

<br><br>
## API列表

### [對外開放的API]
1. 一般銷售案件的下單
    + 一般銷售案件，就是指非高併發的那種銷售案件，不會有短時間大量搶購的商品，由賣家發布該商品的銷售案件。
    + 多個一般銷售案件，可以整合在一張訂單裡面，購物車的概念。
    + 使用CircuitBreaker設置熔斷條件，避免服務雪崩
        + 因為整個訂單新增流程比較耗時(經歷多個I/O與計算)，所以使用斷路器。如果一段時間內出錯太多次，代表可能一次太多人送購物車造成阻塞，就先熔斷此服務，讓伺服器可以慢慢消化之前的任務。
        + 此API在dev環境的斷路器預設配置(針對core-processor單一實例):
            + 每10次調用中，如果其中超過50%遭遇例外，又或者超過30%的慢調用(定義超過3秒執行時間)，則斷路器開啟。
            + 斷路器開啟20秒後，自動切換至半開狀態。
            + 半開狀態下允許5個調用執行正式邏輯，其餘fallback。 
            + 5個調用其中若錯誤/慢調用占比又超過閥值，則斷路器重新開啟，反之則斷路器關閉。
    + 請求範例:
        + ```
          POST /order/normal HTTP/1.1
          Host: localhost:8030
          Content-Type: application/json

          {"goods_list": [{"g_id": 1,"g_name": "螺絲套組","quantity": 20},{"g_id": 2,"g_name": "板手套組","quantity": 50}]}
          ```
        + 購物車下單，買20個螺絲套組 & 50個板手套組
    
2. 快閃銷售案件的下單
    + 快閃銷售案件，就是指高併發的那種銷售案件，例如LiSA的演場會資格商品，由賣家發布該商品的快閃銷售案件，並設定開賣時間
    + 快閃銷售案件不能和其他銷售案件整合在同個訂單裡面，不適用購物車的概念，且一次只能買一個。
    + 使用RateLimiter限制流量，避免服務被打爆。
      + 此API在dev環境的限流器預設配置(針對high-concurrency-processor單一實例):
        + 限制每秒請求數10個。
    + 不用斷路器而用限流器的原因，是因為此功能不能接受斷路器長時間開啟，假如門票12點開賣，11:59因某種原因斷路器被打開，那可能會變成12點到了，搶門票API還沒有提供服務的情況。
    + 請求範例:
       + ```
          POST /order/flash HTTP/1.1
          Host: localhost:8030
          Content-Type: application/json
    
          {"flash_event":{"fse_id":1}}
         ```
       + 下單，搶購一個ID為1的快閃銷售案件

3. 查詢快閃銷售案件的頁面資料
    + 查詢商品資訊，用於渲染快閃銷售案件的頁面
    + 屬於熱點資料，使用redis快取方案
    + 使用RateLimiter限制流量，避免服務被打爆。
      + 不用斷路器而用限流器的原因，是因為此功能不能接受斷路器長時間開啟。假如門票12點開賣，11:59因某種原因斷路器被打開，那可能會變成12點到了，大家還查不到view而不能搶門票的情況。
      + 此API在dev環境的限流器預設配置(針對core-processor單一實例):
        + 限制每秒請求數5個。
    + 請求範例:
        + ```
            GET /flash-sale-event/query/1 HTTP/1.1
            Host: localhost:8030
          ```
        + 查詢ID為1的快閃銷售案件所對應的商品資料

4. 完成一筆訂單
    + 不論是一般銷售案件還是快閃銷售案件，下單成功後都會成立一筆訂單，此時訂單狀態是"進行中"；等到後續賣家發貨了，買家也收貨了，那可以由賣家調整訂單狀態為"已完成"
    + 請求範例:
        + ```
            POST /finish-order HTTP/1.1
            Host: localhost:8030
            Content-Type: application/json

            {"o_id":1}
          ```
        + 更改訂單ID為1的訂單至"已完成"的狀態
    
5. 更改某個一般銷售案件的狀態(上架中/已下架)
    + 被下架的銷售案件，將不會顯示在案件列表的頁面上，會員也就不會有下單的機會。
    + 一般銷售案件的下架，必須要人為設置，沒有定時下架的功能(快閃銷售案件才有)。
    + 請求範例:
       + ```
            POST /sale-event/update-state HTTP/1.1
            Host: localhost:8030
            Content-Type: application/json
            
            {"se_id":1,"is_public":false}
          ```
       + 將ID為1的一般銷售案件，更改為已下架的狀態。

6. 下架某個快閃銷售活動
    + 被下架的銷售案件，將不會顯示在案件列表的頁面上，會員也就不會有下單的機會。
    + 快閃銷售案件有時效問題(例如在上架時就要設定幾天後過期自動下架)，且還涉及redis與mongo等等中間件的資料暫存問題，所以統一設計成一旦下架，那就無法再重新上架，要嘛就廠商根據庫存再創一個新的快閃銷售活動。
    + 請求範例:
        + ```
            POST /flash-sale-event/close HTTP/1.1
            Host: localhost:8030
            Content-Type: application/json
            
            {"fse_id":1}
          ```
        + 將ID為1的快閃銷售案件，更改為已下架的狀態。

<br>

### [開發用的API]
1. 重置所有DB & redis資料
    + 每次重啟core-process的時候都會創建DB & 重置所有資料，這個API的目的就是可以不用每次想重置DB就需重啟core-processor，打個API就好，讓DEMO更方便。
    + 重置不等於清空資料，還會創建最初的DEMO用資料。
    + 請求範例:
       + ```
            GET /inner/system/init HTTP/1.1
            Host: localhost:8001
         ```
       + 重置MySQL、MongoDB、redis。

<br><br>
## 未來可以擴充的地方

1. _登入認證沒有做_

會員的登入認證沒有做，因為不是本專題的重點，目前都是寫死的，未來有時間可以做上去，看是要用JWT或是redis的方式。

2. _跨實例的服務限流與服務熔斷_

resilience4j是實例級別的系統容錯方案，好像只能做到單實例的服務熔斷或限流。如果要以API為單位，實現跨實例的服務熔斷或限流，可能要考慮用中間件去實現，如redis分布式鎖。

3. _分散式日誌追蹤，可以考慮使用Spring Cloud Sleuth_

目前是自己實作過濾鍊，加上為每個請求賦予UUID，來實現分散式日志追蹤。過程中查到Spring Cloud Sleuth好像用起來蠻方便的，未來可以玩玩看。

4. _分散式配置中心沒有做_

有時候服務 & 環境一多，配置檔的管理就變得棘手起來。使用Spring Cloud Config，把配置檔集中到某處統一管理，結合git + MQ，實現配置檔統一版本控管 & 各實例runtime拉取配置檔並讀取最新配置檔。

5. _Message消費失敗的處理邏輯沒有做_

目前(v1.0.x)是設置無應答模式(acknowledge-mode=none)，也就是消費Msg後，不管成功或失敗都會將Msg從Queue裡刪掉，會造成訊息丟失。有空再設計失敗時的處理，含死信機制都要設計一下，目前消費失敗只有印log。

6. _MQ的死信機制沒有做_

Message過期問題以及Message消費途中拋例外造成訊息丟失的問題，有空再設計。