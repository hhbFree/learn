zuul和gateway的区别

zuul（netfilx）与spring-cloud-gateway（apache）的区别：

1、gateway对比zuul多依赖了spring-webflux，内部实现了限流、负载均衡等，扩展性也更强，但同时也限制了仅适合于Spring Cloud套件。
zuul则可以扩展至其他微服务框架中，其内部没有实现限流、负载均衡等。
　　
2、zuul仅支持同步，
　gateway支持异步。

3、gateway线程开销少，支持各种长连接、websocket，spring官方支持，但运维复杂，
zuul编程模型简单,开发调试运维简单，有线程数限制，延迟堵塞会耗尽线程连接资源。
————————————————
版权声明：本文为CSDN博主「FH-Admin」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/u010253246/article/details/117249148