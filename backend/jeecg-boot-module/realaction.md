2）兼容问题
截止SpringAI的1.0.0-M6版本为止，SpringAI的OpenAiModel和阿里云百炼的部分接口存在兼容性问题，包括但不限于以下两个问题：
- 在与AI请求的stream模式中，阿里云百炼返回的tool-arguments是不完整的，需要拼接，而OpenAI则是完整的，无需拼接。
- 音频识别中的数据格式，阿里云百炼的qwen-omni模型要求的参数格式为data:;base64,${media-data}，而OpenAI是直接{media-data}

由于SpringAI的OpenAI模块是遵循OpenAI规范的，所以即便版本升级也不会去兼容阿里云，除非SpringAI单独为阿里云开发starter，所以目前解决方案有两个：
- 等待阿里云官方推出的spring-alibaba-ai升级到最新版本
- 自己重写OpenAiModel的实现逻辑。

首先，我们自己写一个遵循阿里巴巴百炼平台接口规范的ChatModel，其中大部分代码来自SpringAI的OpenAiChatModel，只需要重写接口协议不匹配的地方即可。
从资料\源代码\后端\ai获取AlibabaOpenAiChatModel类，并拷贝到jeecg-boot-module\jeecg-boot-module-airag\src\main\java\org\jeecg\modules\airag\config下。

接下来，我们要把AliababaOpenAiChatModel配置到Spring容器。

修改ChatConfiguration，添加配置：
@Bean
public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention) {
    String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
    String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
    String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
    String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
    Map<String, List<String>> connectionHeaders = new HashMap<>();
    if (StringUtils.hasText(projectId)) {
        connectionHeaders.put("OpenAI-Project", List.of(projectId));
    }

    if (StringUtils.hasText(organizationId)) {
        connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
    }
    RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
    WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
    OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(new SimpleApiKey(apiKey)).headers(CollectionUtils.toMultiValueMap(connectionHeaders)).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
    AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
    Objects.requireNonNull(chatModel);
    observationConvention.ifAvailable(chatModel::setObservationConvention);
    return chatModel;
}

代码说明：
从配置属性中获取 API 连接信息（如 baseUrl、apiKey 等）。
构建 OpenAiApi 客户端，设置请求头和错误处理器。
使用 AlibabaOpenAiChatModel.Builder 创建模型实例，并注入依赖组件（如重试模板、工具管理器等）。
设置可选的监控注册表和观测约定。
返回配置好的聊天模型实例。

更改ChatClient的配置，使用AlibabaOpenAiChatModel ，如下图：
![alt text](image.png)