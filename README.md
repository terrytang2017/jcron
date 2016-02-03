# jcron
Cron expression parser for java

## Maven

``` xml
    <dependency>
       <groupId>com.github.stuxuhai</groupId>
       <artifactId>jcron</artifactId>
       <version>1.0.1</version>
    </dependency>
```

## Usage

``` java
    DateTime now = DateTime.now();
    CronExpression cronExpression = new CronExpression("0 0 12 * * ?");
    DateTime nextTime = cronExpression.getTimeAfter(now);
```
