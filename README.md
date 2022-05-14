# reggie_byShawn

#### 介绍
第一个练手的电商项目。黑马程序员瑞吉外卖项目教程地址：https://www.bilibili.com/video/BV13a411q753?p=156
该项目是一个基于Springboot和MybatisPlus的基础电商项目，其包括后台的管理端和用户点餐端模块。

#### 软件架构
软件架构说明


#### 核心技术栈
项目搭建工具：Maven
版本控制：Git
核心开发框架：springboot + mybatisplus
数据库：mysql,redis(优化)
集成开发工具：intellij IDEA

#### 使用说明

1.克隆项目到本地
2.运行ReggieApplication.java，网页访问即可
3.补充：
后台管理系统：http://localhost:8080/backend/page/login/login.html
前台点餐系统：http://localhost:8080/front/page/login.html

#### 值得学习的一些知识点：
1.数据库密码的md5加密和id的雪花算法生成
2.AntPathMatcher工具字符串匹配
3.静态资源的映射
4.全局异常的处理方式
5.雪花算法导致的Long类型精度损失的处理办法，扩展mvc的消息转换器。
6.优化生成创建和更新时间和用户的办法：公共字段自动填充
    处理方法：
        ①在实体类添加@TableField注解
        ②common包下写元数据对象处理器
        ③在元处理器下获取用户操作线程的id
7.菜品删除检测是否关联套餐。
8.新增菜品时，联合菜品口味的插入。
9.菜品分页查询的关联类型查找。
10.菜品分页查询的类型信息仅能查到前11条数据？暂时未解决
