package com.docsdk.resource.sync;

import com.docsdk.client.mapper.ObjectMapperProvider;
import com.docsdk.client.setttings.SettingsProvider;
import com.docsdk.dto.request.TaskRequest;
import com.docsdk.dto.response.JobResponse;
import com.docsdk.dto.response.Pageable;
import com.docsdk.dto.result.Result;
import com.docsdk.executor.RequestExecutor;
import com.docsdk.resource.AbstractJobsResource;
import com.docsdk.resource.params.Filter;
import com.docsdk.resource.params.Include;
import com.docsdk.resource.params.Pagination;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
public class JobsResource extends AbstractJobsResource<
    Result<JobResponse>, Result<Pageable<JobResponse>>, Result<Void>> {

    private final RequestExecutor requestExecutor;

    public JobsResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<JobResponse> create(
        @NotNull final Map<String, TaskRequest> tasks
    ) throws IOException, URISyntaxException {
        return create(tasks, "");
    }

    @Override
    public Result<JobResponse> create(
        @NotNull final Map<String, TaskRequest> tasks, @NotNull final String tag
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getCreateHttpUriRequest(tasks, tag), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<JobResponse> show(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getShowHttpUriRequest(jobId), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<JobResponse> wait(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getWaitHttpUriRequest(jobId), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<Pageable<JobResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return list(filters, includes, null);
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getListHttpUriRequest(filters, includes, pagination), JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<Void> delete(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getDeleteHttpUriRequest(jobId), VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
