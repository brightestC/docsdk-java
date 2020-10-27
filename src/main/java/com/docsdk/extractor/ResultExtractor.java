package com.docsdk.extractor;

import com.docsdk.client.mapper.ObjectMapperProvider;
import com.docsdk.dto.result.Result;
import com.docsdk.processor.response.DefaultResponseProcessor;
import com.docsdk.processor.response.ResponseProcessor;
import com.docsdk.processor.response.successful.ContentResponseProcessor;
import com.docsdk.processor.response.successful.InputStreamResponseProcessor;
import com.docsdk.processor.response.successful.NoContentResponseProcessor;
import com.docsdk.resource.AbstractResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.io.EmptyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class ResultExtractor {

    private final ResponseProcessor defaultResponseProcessor;
    private final Map<TypeReference<?>, ResponseProcessor> responseProcessors;

    public ResultExtractor(final ObjectMapperProvider objectMapperProvider) {
        final ContentResponseProcessor contentResponseProcessor = new ContentResponseProcessor(objectMapperProvider);
        final NoContentResponseProcessor noContentResponseProcessor = new NoContentResponseProcessor();
        final InputStreamResponseProcessor inputStreamResponseProcessor = new InputStreamResponseProcessor();

        this.defaultResponseProcessor = new DefaultResponseProcessor();
        this.responseProcessors = ImmutableMap.<TypeReference<?>, ResponseProcessor>builder()
            .put(AbstractResource.VOID_TYPE_REFERENCE, noContentResponseProcessor)
            .put(AbstractResource.INPUT_STREAM_TYPE_REFERENCE, inputStreamResponseProcessor)

            .put(AbstractResource.MAP_STRING_TO_OBJECT_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.USER_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.WEBHOOKS_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)

            .put(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor).build();
    }

    public <T> Result<T> extract(
        final HttpResponse httpResponse, final TypeReference<T> typeReference
    ) throws IOException {
        final int status = httpResponse.getStatusLine().getStatusCode();
        final Header[] headers = httpResponse.getAllHeaders();
        final HttpEntity httpEntity = Optional.ofNullable(httpResponse.getEntity()).orElse(new InputStreamEntity(EmptyInputStream.INSTANCE));

        try (final InputStream inputStream = httpEntity.getContent()) {
            if (status >= 200 && status <= 299) {
                return responseProcessors.getOrDefault(typeReference, defaultResponseProcessor).process(status, headers, inputStream, typeReference);
            }

            return defaultResponseProcessor.process(status, headers, inputStream, typeReference);
        }
    }
}
