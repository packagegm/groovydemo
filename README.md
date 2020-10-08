# Groovy内存泄漏问题
## 问题描述
- 在Java中执行Groovy脚本时，每次调用会动态创建类，并加入到Groovy管理的缓存内，在GC时无法释放，最终耗尽PermGen空间，导致OOM
## 优化方案
### 1. 非必要情况不使用Groovy脚本，避免不必要的内存开销 
### 2. 调用Groovy后增加清理GlobalClassValue中的缓存代码
### 3. 增加JVM参数 `-Dgroovy.use.classvalue=true`（需JDK7及以上）

---

# 各优化方案测试
## 测试内容 
- 生成随机数进行除法运算后输出结果
- 循环50000次
- 每次循环暂停1毫秒
## 测试结果
### JDK7环境
- JDK `1.7.0_80` 
- Groovy `2.4.7`
##### 1. 创建普通Java类实现
- 执行完毕，运行时间58.7s
- 未发生GC
    - 伊甸区 46/972.5M，存活区 7.5/324.5M
    - 年老代 121.5M/1.9G
    - 永久代 21/82M，实际占用 4.8M
##### 2. 调用Groovy脚本实现
- 执行过程中因永久代填满中断，运行时间 1m35s
```
>>>>>>>13090
Random is -659628534
The Result is -164907133
>>>>>>>13091
Exception in thread "main" 
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "main"
```
- 总GC47次，10.328秒
    - 伊甸区 683.5/972.5M， 存活区 120/324.5M × 2，PSScavenge GC 31次，1.762s
    - 年老代 1.9/1.9G，PSParallelCompact GC 16次，8.566s
    - 永久代 82/82M
##### 3. 调用Groovy后增加清理代码
- 执行完毕，运行时间 6m25s
- 总GC213次，10.541秒
    - 伊甸区 273/972.5M， 存活区 74/324.5M × 2，PSScavenge GC 205次，6.323s
    - 年老代 1.02/1.9G，实际占用75.5M，PSParallelCompact GC 8次，4.220s
    - 永久代 82/82M，实际占用72M
##### 4. 启用`use.classvalue`
- 执行完毕，运行时间 5m14s
- 总GC99次，24.149秒
    - 伊甸区 331/972.5M， 实际占用 46.585M，存活区 306/324.5M × 2，PSScavenge GC 89次，12.888s
    - 年老代 1.397/1.9G，实际占用217.940M，PSParallelCompact GC 10次，11.261s
    - 永久代 82/82M，实际占用69.940M
### JDK8环境 
- JDK `1.8.0_265`
- Groovy `2.0.6`
##### 1. 调用Groovy脚本实现
##### 2. 调用Groovy后增加清理代码
##### 3. 启用`use.classvalue`
