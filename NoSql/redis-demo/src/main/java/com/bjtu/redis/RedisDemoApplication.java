package com.bjtu.redis;

import com.bjtu.redis.ActionExecutor;
import com.bjtu.redis.ActionJsonLoader;
import com.bjtu.redis.CounterJsonLoader;
import com.bjtu.redis.MyJsonMonitor;
import com.bjtu.redis.MyLock;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *  SpringBootApplication
 * 用于代替 @SpringBootConfiguration（@Configuration）、 @EnableAutoConfiguration 、 @ComponentScan。
 * <p>
 * SpringBootConfiguration（Configuration） 注明为IoC容器的配置类，基于java config
 * EnableAutoConfiguration 借助@Import的帮助，将所有符合自动配置条件的bean定义加载到IoC容器
 * ComponentScan 自动扫描并加载符合条件的组件
 */
@SpringBootApplication
public class RedisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);

        /*加载counter和action的json文件*/
        ActionJsonLoader.load();
        CounterJsonLoader.load();

        /*加载文件修改监听monitor*/
        File directory = new File(new File("."),"src/main/resources");
        long interval = TimeUnit.SECONDS.toMillis(1);
        FileAlterationObserver observer = new FileAlterationObserver(
                directory, FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".json")));
        observer.addListener(new MyJsonMonitor());
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval,observer);
        try {
            monitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        /*打印菜单*/
        System.out.println();
        System.out.println("Welcome to use the Redis-based Counter.");
        System.out.println("Author: Harry Zhou.");
        System.out.println("All Rights Reserved.");

        while (true){
            System.out.println("----------Redis-Based Counter----------");
            System.out.println("Here is the function menu:");
            System.out.println("A. increase count \t B. get count \nC. get freq \t\t D. get desc \nE. set desc \t\t F. get write action log \nQ. quit");
            System.out.print("Please input your choice here >>");
            Scanner sc = new Scanner(System.in);
            String choice = sc.nextLine();
            while(!choice.equals("A")&&!choice.equals("B")&&!choice.equals("a")&&!choice.equals("b")
                    &&!choice.equals("C")&&!choice.equals("c")&&!choice.equals("D")&&!choice.equals("d")
                    &&!choice.equals("E")&&!choice.equals("e")&&!choice.equals("F")&&!choice.equals("f")) {
                if (choice.equals("Q")||choice.equals("q")){    //退出
                    System.out.println("Bye~");
                    System.exit(0);
                }
                System.out.print("Wrong input! please input again >>"); //错误输入则循环
                choice = sc.nextLine();
            }
            MyLock.getReadLock();   //设置锁
            switch (choice){
                case "A":
                case "a":   //计数
                    ActionExecutor executor1 = new ActionExecutor(ActionJsonLoader.getAction("incrcount"),CounterJsonLoader.getMyCounterMap());   //执行action
                    executor1.execute();
                    break;
                case "B":
                case "b":   //读数
                    ActionExecutor executor2 = new ActionExecutor(ActionJsonLoader.getAction("getcount"),CounterJsonLoader.getMyCounterMap());   //执行json
                    executor2.execute();
                    break;
                case "C":
                case "c":   //获取frequent计数
                    ActionExecutor executor3 = new ActionExecutor(ActionJsonLoader.getAction("getfreq"),CounterJsonLoader.getMyCounterMap());   //执行json
                    executor3.execute();
                    break;
                case "D":
                case "d":   //获得description
                    ActionExecutor executor4 = new ActionExecutor(ActionJsonLoader.getAction("getdesc"),CounterJsonLoader.getMyCounterMap());   //执行json
                    executor4.execute();
                    break;
                case "E":
                case "e":   //设置description
                    System.out.print("Please enter the new description >>");
                    String desc = sc.nextLine();
                    ActionExecutor executor5 = new ActionExecutor(ActionJsonLoader.getAction("setdesc"),CounterJsonLoader.getMyCounterMap());   //执行json
                    executor5.setDesc(desc);
                    executor5.execute();
                    break;
                case "F":
                case "f":   //获得写入操作日志
                    ActionExecutor executor6 = new ActionExecutor(ActionJsonLoader.getAction("getlog"),CounterJsonLoader.getMyCounterMap());   //执行json
                    executor6.execute();
                    break;
            }
            MyLock.freeReadLock();  //释放锁
        }
    }
}
