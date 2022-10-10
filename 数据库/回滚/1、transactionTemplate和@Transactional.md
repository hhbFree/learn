1. transactionTemplate

1.1 ransactionTemplate.execute范围内都会回滚，try catch的异常域也会回滚

1.2 双数据库下只有一个生效

1.3 方法中需要重写 transactionTemplate 不需要上提到接口，在方法中也能执行回滚，不重写transactionTemplate 不回滚

1.4 接口中调用也能执行回滚，不管接口几层结构，都是可以回滚的

1.5 错误数据需要 throw new  RuntimeException(e) 异常，不然不会回滚

2.@Transactional(rollbackFor = Exception.class)

1.1 方法范围内全部会回滚

1.2 双数据库下只有一个生效

1.3 方法不重写接口。不添加注解@Transactional，也是可以回滚的

1.4接口中调用也能执行回滚，不管接口几层结构，都是可以回滚的

1.5 错误数据需要 throw new  RuntimeException(e) 异常，不然不会回滚

javax.transaction.Transactional 与 Spring 的Transactional的区别
https://www.zhaozhizheng.com/articles/2018/06/28/1530198427784.html


```$java
 @Service
 public class VmsStatisticsCommonServiceImpl implements VmsStatisticsCommonService {
 
     @Autowired
     private RollbackService rollbackService;
 
     @Resource
     private VmsStatisticsTypeDAO vmsStatisticsTypeDAO;
 
     @Resource
     private RollBack rollBack;
 
     @Resource
     private VmsStatisticsDAO vmsStatisticsDAO;
 
  
 
     /**
      * 模板
      * @param type
      */
     @Override
     public void rollback(Integer type) {
         try {
 
 
             add( type);
 
             addMethod( type);
 
             //存入
             VmsStatisticsTypeDomain vmsStatisticsTypeDomain5=new VmsStatisticsTypeDomain();
             vmsStatisticsTypeDomain5.setCreateDate(new Date());
             vmsStatisticsTypeDomain5.setStatisticsType("test5");
             vmsStatisticsTypeDAO.add(vmsStatisticsTypeDomain5);
 
             transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                 @Override
                 protected void doInTransactionWithoutResult(TransactionStatus status) {
                     try {
 
                         //不存入
                         VmsStatisticsTypeDomain vmsStatisticsTypeDomain1=new VmsStatisticsTypeDomain();
                         vmsStatisticsTypeDomain1.setCreateDate(new Date());
                         vmsStatisticsTypeDomain1.setStatisticsType("test1");
                         vmsStatisticsTypeDAO.add(vmsStatisticsTypeDomain1);
                         //另一个数据库 存入
                        // rollBack.add();
 
                         //同数据库中一张表不影响 不存入
                         VmsStatisticsDomain vmsStatisticsDomain=new VmsStatisticsDomain();
                         vmsStatisticsDomain.setCreateDate(new Date());
                         vmsStatisticsDomain.setStatisticsTypeId(1);
                         vmsStatisticsDomain.setValidationDate(new Date());
                         vmsStatisticsDAO.add(vmsStatisticsDomain);
 
                         //调用接口
                         rollbackService.add();
 
                         int i =1/type;
                     } catch (Exception e) {
                         //不存入
                         System.out.println("rollback before");
                         VmsStatisticsTypeDomain vmsStatisticsTypeDomain2=new VmsStatisticsTypeDomain();
                         vmsStatisticsTypeDomain2.setCreateDate(new Date());
                         vmsStatisticsTypeDomain2.setStatisticsType("test2");
                         vmsStatisticsTypeDAO.add(vmsStatisticsTypeDomain2);
 
                         // 事务回滚
                         status.setRollbackOnly();
 
                         //不存入
                         System.out.println("rollback after");
                         VmsStatisticsTypeDomain vmsStatisticsTypeDomain3=new VmsStatisticsTypeDomain();
                         vmsStatisticsTypeDomain3.setCreateDate(new Date());
                         vmsStatisticsTypeDomain3.setStatisticsType("test3");
                         vmsStatisticsTypeDAO.add(vmsStatisticsTypeDomain3);
                     }
                 }
             });
         } catch (Exception e) {
             //不会捕捉到
             System.out.println("rollback out");
 
         }
 
         //存入
         VmsStatisticsTypeDomain vmsStatisticsTypeDomain4=new VmsStatisticsTypeDomain();
         vmsStatisticsTypeDomain4.setCreateDate(new Date());
         vmsStatisticsTypeDomain4.setStatisticsType("test4");
         vmsStatisticsTypeDAO.add(vmsStatisticsTypeDomain4);
     }
 
     /**
      * 注解
      * @param type
      */
     @Override
     @Transactional(rollbackFor = Exception.class)
     public void rollbackAnnotation(Integer type) {
         //另一个数据库 存入
         rollBack.add();
 
         //不存入可以回滚
         addMethod(type);
 
         //调用接口
 //        rollbackService.add();
 
 
 
     }
 
     private void addMethod(Integer type) {
         //不存入
         VmsStatisticsDomain vmsStatisticsDomain=new VmsStatisticsDomain();
         vmsStatisticsDomain.setCreateDate(new Date());
         vmsStatisticsDomain.setStatisticsTypeId(3);
         vmsStatisticsDomain.setValidationDate(new Date());
         vmsStatisticsDAO.add(vmsStatisticsDomain);
     }
 
     //不需要接口
     public void add(Integer type){
         transactionTemplate.execute(new TransactionCallbackWithoutResult() {
             @Override
             protected void doInTransactionWithoutResult(TransactionStatus status) {
                 try {
                     //另一个数据库 存入
                     rollBack.add();
 
                     //不存入
                     VmsStatisticsDomain vmsStatisticsDomain=new VmsStatisticsDomain();
                     vmsStatisticsDomain.setCreateDate(new Date());
                     vmsStatisticsDomain.setStatisticsTypeId(1);
                     vmsStatisticsDomain.setValidationDate(new Date());
                     vmsStatisticsDAO.add(vmsStatisticsDomain);
 
                     int i =1/0;
                 } catch (Exception e) {
                     //不存入
                     System.out.println("add before");
                     // 事务回滚
                     status.setRollbackOnly();
 
                 }
             }
         });
     }
 
 }


@Service
public class RollbackServiceImpl implements RollbackService {

    @Resource
    private VmsStatisticsDAO vmsStatisticsDAO;

    @Autowired
    private RollbackService2 rollbackService2;

    @Override
    public void add() {
        //不存入
        VmsStatisticsDomain vmsStatisticsDomain=new VmsStatisticsDomain();
        vmsStatisticsDomain.setCreateDate(new Date());
        vmsStatisticsDomain.setStatisticsTypeId(2);
        vmsStatisticsDomain.setValidationDate(new Date());
        vmsStatisticsDAO.add(vmsStatisticsDomain);

        rollbackService2.add();
    }
}


@Service
public class Rollback2ServiceImpl implements RollbackService2 {

    @Resource
    private VmsStatisticsDAO vmsStatisticsDAO;

    @Override
    public void add() {
        //不存入
        try {
            VmsStatisticsDomain vmsStatisticsDomain=new VmsStatisticsDomain();
            vmsStatisticsDomain.setCreateDate(new Date());
            vmsStatisticsDomain.setStatisticsTypeId(4);
            vmsStatisticsDomain.setValidationDate(new Date());
            vmsStatisticsDAO.add(vmsStatisticsDomain);
            int i=1/0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new  RuntimeException(e);
        }
    }
}
```