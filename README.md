# springboot-swagger-ui

名字待定....

### 组织结构

```
src/
├── main/
│   ├── java/
│   │   └── com.abel.example/
│   │       ├── common/               # 公共组件
│   │       │   ├── enums/            # 枚举定义
│   │       │   │   └── ResultEnum.java
│   │       │   └── util/             # 工具类
│   │       │       ├── ProgressInputStream.java
│   │       │       ├── ProgressTracker.java
│   │       │       └── Utils.java
│   │       ├── config/               # 配置类
│   │       │   └── BeanConfiguration.java
│   │       ├── controller/           # 控制器层
│   │       │   ├── FileController.java
│   │       │   ├── LoginController.java
│   │       │   ├── OllamaController.java
│   │       │   ├── PdfController.java
│   │       │   └── RagController.java
│   │       ├── dao/                  # 数据访问层
│   │       ├── interceptor/          # 拦截器
│   │       ├── model/                # 数据模型
│   │       ├── service/              # 业务服务层
│   │       │   ├── file/             # 文件服务
│   │       │   │   ├── FileService.java
│   │       │   │   └── FileServiceImpl.java
│   │       │   ├── mail/             # 邮件服务
│   │       │   │   ├── MailService.java
│   │       │   │   └── MailServiceImpl.java
│   │       │   ├── ollama/           # Ollama集成服务
│   │       │   │   ├── OllamaService.java
│   │       │   │   └── OllamaServiceImpl.java
│   │       │   ├── pdf/              # PDF处理服务
│   │       │   │   ├── PdfChunkService.java
│   │       │   │   ├── PdfChunkServiceImpl.java
│   │       │   │   ├── PdfService.java
│   │       │   │   └── PdfServiceImpl.java
│   │       │   ├── rag/              # RAG服务
│   │       │   │   ├── RagService.java
│   │       │   │   └── RagServiceImpl.java
│   │       │   ├── user/            # 用户服务
│   │       │   │   ├── UserService.java
│   │       │   │   └── UserServiceImpl.java
│   │       │   └── weaviate/        # Weaviate集成服务
│   │       │       ├── WeaviateService.java
│   │       │       └── WeaviateServiceImpl.java
│   │       └── Application.java      # 主启动类
│   └── resources/                    # 资源文件
│       └── application.properties    # 应用配置文件
```










