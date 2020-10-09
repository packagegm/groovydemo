# Groovy内存泄漏问题
## 问题描述
- 在Java中执行Groovy脚本时，每次调用会动态创建类，并加入到Groovy管理的缓存内，在GC时无法释放，最终耗尽PermGen空间，导致OOM
## 优化方案
1. 调用Groovy后增加清理GlobalClassValue中的缓存代码（JDK8需增加`-XX:MaxMetaspaceSize=256M`）
2. 增加JVM参数 `-Dgroovy.use.classvalue=true`（需JDK7及以上）
3. 缓存并复用同内容的Script对象，将创建的动态类控制在固定值
## 各优化方案测试
### 测试内容 
- 生成随机数进行除法运算后输出结果
- 循环50000次
- 每次循环暂停1毫秒
### JDK7环境测试结果
##### 环境
- JDK `1.7.0_80` 
- Groovy `2.4.7`
##### 创建普通Java类实现
- 执行完毕，运行时间58.7s
- 未发生GC
    - 伊甸区 46/972.5M，存活区 7.5/324.5M
    - 年老代 121.5M/1.9G
    - 永久代 21/82M，实际占用 4.8M
##### 调用Groovy脚本实现
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
##### 调用Groovy后增加清理代码
- 执行完毕，运行时间 6m25s
- 总GC213次，10.541秒
    - 伊甸区 273/972.5M， 存活区 74/324.5M × 2，PSScavenge GC 205次，6.323s
    - 年老代 1.02/1.9G，实际占用75.5M，PSParallelCompact GC 8次，4.220s
    - 永久代 82/82M，实际占用72M
##### 启用`use.classvalue`
- 执行完毕，运行时间 5m14s
- 总GC99次，24.149秒
    - 伊甸区 331/972.5M， 实际占用 46.585M，存活区 306/324.5M × 2，PSScavenge GC 89次，12.888s
    - 年老代 1.397/1.9G，实际占用217.940M，PSParallelCompact GC 10次，11.261s
    - 永久代 82/82M，实际占用69.940M
##### 缓存Script
- 增加调用次数到100000
- 执行完毕，运行时间 2m14s
- 总GC184次，0.434秒
    - 伊甸区 27/972M， 实际占用 11.930M，存活区 0.512/324M × 2，PSScavenge GC 184次，0.434s
    - 年老代 121/1901M，实际占用 6.076M，PSParallelCompact GC 0次，0s
    - 永久代 21/82M，实际占用13.044M
> 从5万次后永久代稳定保持在13M
### JDK8环境测试结果
##### 环境 
- JDK `1.8.0_265`
- Groovy `2.0.6`
##### 调用Groovy脚本实现
- 执行完毕，运行时间 8m32s
- 总GC96次，33.071秒
    - 伊甸区 330/972.5M， 实际占用 303.644M，存活区 317/324.5M × 2，PSScavenge GC 88次，13.499s
    - 年老代 1.397/1.9G，实际占用217.940M，PSParallelCompact GC 8次，19.571s
    - 元空间 499.789/1014M，实际占用289.488M
> 元空间与年老代虽未占满，但GC后均未释放，调用量增加后会一直增加直至达到空间上限
##### 调用Groovy后增加清理代码
- 执行完毕，运行时间 6m33s
- 总GC140次，10.657秒
    - 伊甸区 592/972M，存活区 159/324M × 2，PSScavenge GC 134次，5.835s
    - 年老代 505.5M/1.9G，实际占用56.359M，PSParallelCompact GC 6次，4.823s
    - 元空间 70/1096M，实际占用15.177M
##### 启用`use.classvalue`
- 执行完毕，运行时间 6m25s
- 总GC100次，27.473秒
    - 伊甸区 334/972M， 实际占用 192.898M，存活区 317/324M × 2，PSScavenge GC 91次，12.712s
    - 年老代 849/1901M，实际占用 57.786M，PSParallelCompact GC 9次，14.762s
    - 元空间 74/1014M，实际占用17.759M
##### 缓存Script
- 增加调用次数到100000
- 执行完毕，运行时间 1m7s
- 总GC89次，0.277秒
    - 伊甸区 27/972M， 实际占用 3.827M，存活区 0.512/324M × 2，PSScavenge GC 89次，0.227s
    - 年老代 123/1901M，实际占用 5.206M，PSParallelCompact GC 0次，0s
    - 元空间 13.375/1014M，实际占用13.001M
> 从5万次后元空间一直稳定占用13M
## 结论
- 在使用Groovy脚本较多的情况下，优先使用缓存Script方式
- 缓存Script后仍无法unload动态创建的类，如占用过多，可增加JVM参数或使用清理语句在触发FGC时回收（JDK8下需指定MaxMetaspaceSize）
