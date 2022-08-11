## mysql乐观锁
 ![image](分布式/image/20181227120505598.png)
 
 相信很多朋友在面试的时候，都会被问到乐观锁和悲观锁的问题，如果不清楚其概念和用法的情况下，相信很多朋友都会感觉很懵逼，那么面试的结果也就不言而喻了。
 那么乐观锁和悲观锁到底是个什么东西，用它能来做什么呢？
 相信大家都遇到这种场景，当很多人（一两个人估计不行）同时对同一条数据做修改的时候，那么数据的最终结果是怎样的呢？
 这也就是我们说的并发情况，这样会导致以下两种结果：
 
 更新错误，你修改之后的数据可能被别人覆盖了，导致你很懵逼，甚至怀疑自己开发的功能是否有问题；
 脏读，数据更新错误，导致读数据也是错的，查询出一些默认奇妙的数据，看到的不是你自己修改的结果。
 这样的问题怎么解决呢？于是乎，锁就这样产生了，锁分为乐观锁和悲观锁，它的目的是用来解决并发控制的问题。
 MyISAM引擎不支持事务，所以不考虑它有乐观锁和悲观锁概念。MyISAM只有表锁,锁又分为读锁和写锁。在这里我们只讨论InnoDB引擎。
 需要注意的是，乐观锁和悲观锁并不是解决并发控制的唯一手段（也可以使用消息中间件kafka，MQ之类的作为缓冲等等），而且乐观锁和悲观锁并不仅限制在mysql中使用，它是一种概念，很多其他的应用，如redis，memcached等，只要存在并发情况的，都可以应用这种概念，只是方式上有些差别而已。
 
 
 一、乐观锁
 乐观锁，简单地说，就是从应用系统层面上做并发控制，去加锁。
 实现乐观锁常见的方式：版本号version
 实现方式，在数据表中增加版本号字段，每次对一条数据做更新之前，先查出该条数据的版本号，每次更新数据都会对版本号进行更新。在更新时，把之前查出的版本号跟库中数据的版本号进行比对，如果相同，则说明该条数据没有被修改过，执行更新。如果比对的结果是不一致的，则说明该条数据已经被其他人修改过了，则不更新，客户端进行相应的操作提醒。
 使用版本号实现乐观锁
 使用版本号时，可以在数据初始化时指定一个版本号，每次对数据的更新操作都对版本号执行+1操作。并判断当前版本号是不是该数据的最新的版本号。 
 
 //1.查询出商品信息 
 
 select status,version from t_goods where id=#{id} 
 
 //2.根据商品信息生成订单 
 
 //3.修改商品status为2 
 
 update t_goods 
 
 set status=2,version=version+1 
 
 where id=#{id} and version=#{version};
 
 
 注意第二个事务执行update时，第一个事务已经提交了，所以第二个事务能够读取到第一个事务修改的version。
 
 下面这种极端的情况：
 
 我们知道MySQL数据库引擎InnoDB，事务的隔离级别是Repeatable Read，因此是不会出现脏读、不可重复读。
 在这种极端情况下，第二个事务的update由于不能读取第一个事务未提交的数据（第一个事务已经对这一条数据加了排他锁，第二个事务需要等待获取锁），第二个事务获取了排他锁后，会发现version已经发生了改变从而提交失败。
 
 二、悲观锁
 悲观锁，简单地说，就是从数据库层面上做并发控制，去加锁。
 悲观锁的实现方式有两种：共享锁（读锁）和排它锁（写锁）
 共享锁（IS锁），实现方式是在sql后加LOCK IN SHARE MODE，比如SELECT ... LOCK IN SHARE MODE，即在符合条件的rows上都加了共享锁，这样的话，其他session可以读取这些记录，也可以继续添加IS锁，但是无法修改这些记录直到你这个加锁的session执行完成(否则直接锁等待超时)。
 排它锁（IX锁），实现方式是在sql后加FOR UPDATE，比如SELECT ... FOR UPDATE ，即在符合条件的rows上都加了排它锁，其他session也就无法在这些记录上添加任何的S锁或X锁。如果不存在一致性非锁定读的话，那么其他session是无法读取和修改这些记录的，但是innodb有非锁定读(快照读并不需要加锁)，for update之后并不会阻塞其他session的快照读取操作，除了select ...lock in share mode和select ... for update这种显示加锁的查询操作。
 
 通过对比，发现for update的加锁方式无非是比lock in share mode的方式多阻塞了select...lock in share mode的查询方式，并不会阻塞快照读。
 
 mysql InnoDB引擎默认的修改数据语句:update,delete,insert都会自动给涉及到的数据加上排他锁,select语句默认不会加任何锁类型。
 
 在Java中，synchronized的思想也是悲观锁。
 
 以排它锁为例
 
 要使用悲观锁，我们必须关闭mysql数据库的自动提交属性，因为MySQL默认使用auto commit模式，也就是说，当你执行一个更新操作后，MySQL会立刻将结果进行提交。set autocommit=0; 
 
 //0.开始事务 
 
 begin;/begin work;/start transaction; (三者选一就可以) 
 
 //1.查询出商品信息 
 
 select status from t_goods where id=1 for update; 
 
 //2.根据商品信息生成订单 
 
 insert into t_orders (id,goods_id) values (null,1); 
 
 //3.修改商品status为2 
 
 update t_goods set status=2; 
 
 //4.提交事务 
 
 commit;/commit work;
 
 
```$java

//service
@Transactional
	public void updateWithTimePessimistic(UserInfo entity, int time) throws InterruptedException {		
		UserInfo userInfo = userInfoDao.findByUserNameForUpdate(entity.getUserName());
		if (userInfo == null)
			return;

		if (!StringUtils.isEmpty(entity.getMobile())) {
			userInfo.setMobile(entity.getMobile());
		}
		if (!StringUtils.isEmpty(entity.getName())) {
			userInfo.setName(entity.getName());
		}
		Thread.sleep(time * 1000L);

		userInfoDao.update(userInfo);
	}


```

```$xslt
//dao
@Select({
		"<script>",
	        "SELECT ",
	        "user_name as userName,passwd,name,mobile,valid, user_type as userType, version as version",
	        "FROM user_info_test",
	        "WHERE user_name = #{userName,jdbcType=VARCHAR} for update",
	   "</script>"})
	UserInfo findByUserNameForUpdate(@Param("userName") String userName);
	
	@Update({
        "<script>",
        " update user_info_test set",
        " name = #{name, jdbcType=VARCHAR}, mobile = #{mobile, jdbcType=VARCHAR},version=version+1 ",
        " where user_name=#{userName}",
        "</script>"
    })
    int update(UserInfo userInfo);

```
 
 
 
 上面的查询语句中，我们使用了select…for update的方式， 这样就通过开启排他锁的方式实现了悲观锁。此时在t_goods表中，id为1的 那条数据就被我们锁定了，其它的事务必须等本次事务提交之后才能执行。这样我们可以保证当前的数据不会被其它事务修改。
 
 补充：
 1.MyISAM在执行查询语句(SELECT)前,会自动给涉及的所有表加读锁,在执行更新操作 (UPDATE、DELETE、INSERT等)前,会自动给涉及的表加写锁。
 2.MySQL InnoDB默认行级锁。 行级锁都是基于索引的，如果一条SQL语句用不到索引是不会使用行级锁的，会使用表级锁把整张表锁住。
 3.从上面对两种锁的介绍，我们知道两种锁各有优缺点，不可认为一种好于另一种，像乐观锁适用于写比较少的情况下（多读场景），即冲突真的很少发生的时候，这样可以省去了锁的开销，加大了系统的整个吞吐量。但如果是多写的情况，一般会经常产生冲突，这就会导致上层应用会不断的进行retry，这样反倒是降低了性能，所以一般多写的场景下用悲观锁就比较合适。
 
 参考：
 http://www.cnblogs.com/exceptioneye/p/5373477.html
