eureka

一、缓存存储格式 ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>

![img](https://img-blog.csdnimg.cn/20201009161840300.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxMDQ3MjQ1,size_16,color_FFFFFF,t_70)

 

二、eureka 服务端缓存

readOnlyCacheHashMap

ConcurrentHashMap,定时从readWriteCacheMap同步数据，默认30s

readWriteCacheHahMap

Guava缓存，数据主要同步于存储层。当获取缓存时判断缓存中是否没有数据，如果不存在此数据，则通过 CacheLoader 的 load 方法去加载，加载成功之后将数据放入缓存，同时返回数据。默认180s过期，当服务下线、过期、注册、状态变更，都会来清除此缓存中的数据。

eureka 客户端缓存

eureka Client 启动时会全量拉取服务列表，启动后每隔 30 秒从 Eureka Server 量获取服务列表信息，并保持在本地缓存中。

ribbon 如果使用ribbon负载均衡，ribbon也有30s缓存

缓存工作方式：

![多级缓存工作状态](https://img-blog.csdnimg.cn/20201009161734701.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxMDQ3MjQ1,size_16,color_FFFFFF,t_70)

 

优雅停机时默认最长感应时间 30(Ribbon) + 30(client) + 30(readOnlyCacheMap) + 180(readWriteCacheMap) = 270s
————————————————
版权声明：本文为CSDN博主「然并卵0808」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_31047245/article/details/108980856