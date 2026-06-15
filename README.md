To reproduce:
```
export AZURE_OPENAI_RESOURCE_NAME=...
export AZURE_OPENAI_API_KEY=...

mvn clean package -DskipTests
mvn quarkus:dev
```

then:
```
curl -X POST localhost:18080/hello/chat
```

go to `http://localhost:<langfuse dev service port>/project/quarkus-dev-project`
go to `Tracing`.
you should see a trace.
the last node should print: `completion null`.

`null` comes from `SpanChatModelListener` in this code: 
```
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        ChatRequest request = requestContext.chatRequest();
        Span span = tracer.spanBuilder("completion " + request.parameters().modelName())
                .setAttribute("gen_ai.request.model", request.parameters().modelName())
                .setAttribute("gen_ai.request.temperature",
                        request.parameters().temperature() != null ? request.parameters().temperature() : 0D)
                .setAttribute("gen_ai.request.top_p", request.parameters().topP() != null ? request.parameters().topP() : 0D)
                .startSpan();
        Scope scope = span.makeCurrent();
```

and the `modelName` is null in `request.parameters`.

this seems to be coming from https://github.com/quarkiverse/quarkus-langchain4j/blob/1.10.0/core/runtime/src/main/java/io/quarkiverse/langchain4j/runtime/aiservice/AiServiceMethodImplementationSupport.java#L703-L704 not setting up a model name.

