# API网关

## 设计目标
### 支持dubbo接口转http的方式对外暴露
### 支持可配置式的组件自由选配装载
### 支持自定义扩展功能点，自由挂载自定义功能
### 支持内部http协议的接口通过网关对外透传
### 良好的文档解决方案
### 支持基于prometheus的监控

## 使用说明

## 启用网关
最少需要依赖gateway-core,gateway-register-nacos等2个模块。

#### 添加依赖:

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    open: true
    root-path:
      list:
        - /service
```

#### 配置解释：

    aurora.gateway.open：是否启用网关，该开关决定是否初始化网关相关的配置。
    aurora.gateway.root-path：表示dubbo转http协议能接受的请求根路径列表。

## 注册中心
仅允许依赖一个注册中心实现，会自动初始化注册中心，如果依赖多个实现，会无法启动。
### nacos
基于nacos实现的注册中心。

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-register-nacos</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    register:
      nacos:
        group: gateway
        server-addr: localhost:8848
        timeout-ms: 3000
```

#### 配置解释：

    aurora.gateway.register.nacos.group：分组
    aurora.gateway.register.nacos.server-addr：地址
    aurora.gateway.register.nacos.timeout-ms：超时时间(ms)

#### 配置示例：
在nacos中的配置项做如下配置：

dataId: service-item

group: gateway

配置内容：

{"gw.dns":"{\"interfaceName\":\"com.aurora.gateway.demo.DemoInterface\",\"methodName\":\"invoke\",\"serviceAlias\":\"gw.dns\",\"paramDefinitionList\":[{\"paramName\":\"host\",\"paramType\":\"com.aurora.gateway.demo.Param\",\"subParamDefinitionList\":[{\"paramName\":\"host\",\"paramType\":\"java.lang.String\"}]}]} "}

因为基于nacos内置的差异比对，其要求map结构，故需要注册内容时按这个要求格式来处理。
    
## web filter组件
### 跨域

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-webfilter-cors</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    open: true
    cors:
      enabled: true
```

#### 配置解释：

    aurora.gateway.cors.enabled：是否启用跨域，该开关决定是否初始化跨域相关组件。

#### 自定义扩展：
支持使用方扩展CorsProcessor，仅需自行声明并实现CorsProcessor的bean实例，网关会自行识别并将其注入跨域处理流程中。

### 容灾

#### 原理
首先，定义每一个服务接口定义的失败率&对应的容灾响应。
容灾组件会监听core模块的invoke结果消息，在组件内部会创建每一个声明容灾服务对应的一组统计桶实例。
以（当前时间的毫秒值 / 1000 ） & 桶数量 得出统计消息写入的桶下标，在具体的的桶下标中执行具体成功&失败累计。
在到达一个指定的时间周期后，会重置选中的一个桶，防止统计桶的数据腐化。
最后，在判断是否需要容灾时，累加所有的桶的成功失败值计算失败率，满足失败率阈值，则返回容灾内容。

因为是通过接收消息来执行状态通知的统计的，目前仅支持了基于dubbo invoke的统计。针对http请求透传的统计，需要再合适的位置自行扩展并发送执行结果通知，从而驱动阈值统计。

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-webfilter-circuit-breaker</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    open: true
    circuit-breaker:
      enabled: true
      bucket-nums: 32
      time-interval: 100
```

#### 配置解释：

    aurora.gateway.circuit-breaker.enabled：是否启用容灾，该开关决定是否初始化容灾相关组件。
    aurora.gateway.circuit-breaker.bucket-nums：容灾计算桶数量，要求是2的倍数。
    aurora.gateway.circuit-breaker.time-interval：容灾重置桶的周期，单位ms。
    
### 限流

#### 原理
基于guava的RateLimiter实现。

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-webfilter-rate-limit</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    open: true
    rate-limit:
      enabled: true
```

#### 配置解释：

    aurora.gateway.rate-limit.enabled：是否启用限流，该开关决定是否初始化限流相关组件。
    
### 降级
#### 原理
通过降级名单，识别当前请求，对访问流量降级处理。

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-webfilter-demote</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    open: true
    demote:
      enabled: true
```

#### 配置解释：

    aurora.gateway.demote.enabled：是否启用降级，该开关决定是否初始化降级相关组件。

## 文档
类似swagger机制，围绕网关关注的内容，基于dubbo接口配置，生成服务接口文档数据。

启动后访问文档的地址：

http://localhost:8081/{rootPath}/gateway/api-docs

#### 添加依赖：

```
<dependency>
    <groupId>com.aurora</groupId>
    <artifactId>gateway-doc</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
#### 在application.yml文件中作如下配置：

```
aurora:
  gateway:
    doc:
      app-name: demo
      scan-package:
        - com.aurora.gateway.demo
      enabled: true
```

#### 配置解释：

    aurora.gateway.doc.app-name：应用名称
    aurora.gateway.doc.scan-package：扫描包列表
    aurora.gateway.doc.enabled：是否启用网关文档