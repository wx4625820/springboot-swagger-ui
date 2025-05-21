# springboot-swagger-ui
名字待定....


### 组织结构
```
springboot-swagger-ui/
├── .idea/                # IDEA项目配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.abel.example/
│   │   │       ├── common/       # 公共类/工具类
│   │   │       ├── config/       # 配置类
│   │   │       │   └── OpenApiConfig.java  # Swagger配置
│   │   │       ├── controller/   # 控制器
│   │   │       ├── dao/          # 数据访问层
│   │   │       ├── interceptor/  # 拦截器
│   │   │       ├── model/        # 数据模型
│   │   │       └── service/      # 服务层
│   │   │           ├── file/     # 文件服务相关
│   │   │           │   ├── FileService.java
│   │   │           │   ├── MinIOServiceImpl.java  # MinIO实现
│   │   │           │   └── QiniuServiceImpl.java  # 七牛云实现
│   │   │           └── UserService.java
│   │   │               └── UserServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties  # 应用配置
│   │       └── banner.txt       # 启动banner
│   └── test/                   # 测试代码
├── target/                     # 构建输出目录
├── .gitignore                  # Git忽略规则
├── pom.xml                     # Maven配置
└── README.md                   # 项目说明












