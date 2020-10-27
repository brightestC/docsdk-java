package com.docsdk.resource;

import com.docsdk.client.mapper.ObjectMapperProvider;
import com.docsdk.client.setttings.SettingsProvider;
import com.docsdk.dto.request.ConvertFilesTaskRequest;
import com.docsdk.dto.response.OperationResponse;
import com.docsdk.dto.response.Pageable;
import com.docsdk.dto.response.TaskResponse;
import com.docsdk.dto.result.AbstractResult;
import com.docsdk.resource.params.Filter;
import com.docsdk.resource.params.Include;
import com.docsdk.resource.params.converter.AlternativeToNameValuePairsConverter;
import com.docsdk.resource.params.converter.FiltersToNameValuePairsConverter;
import com.docsdk.resource.params.converter.IncludesToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public abstract class AbstractConvertFilesResource<TRAR extends AbstractResult<TaskResponse>,
    ORP extends AbstractResult<Pageable<OperationResponse>>> extends AbstractResource {

    public static final String PATH_SEGMENT_CONVERT = "convert";
    public static final String PATH_SEGMENT_FORMATS = "formats";

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final AlternativeToNameValuePairsConverter alternativeToNameValuePairsConverter;

    public AbstractConvertFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.alternativeToNameValuePairsConverter = new AlternativeToNameValuePairsConverter();
    }

    /**
     * Create a task to convert one input file from input_format to output_format. Requires the task.write scope.
     *
     * @param convertFilesTaskRequest {@link ConvertFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getConvertHttpUriRequest(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT));
        final HttpEntity httpEntity = getHttpEntity(convertFilesTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * List all supported formats, their engines and possible options.
     *
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats() throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters (optional) Filters:
     *                - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                - engine - Filter result to only include conversions with a matching engine name.
     *                - engine_version - Filter result to only include conversions with a matching engine version.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters  (optional) Filters:
     *                 - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                 - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                 - engine - Filter result to only include conversions with a matching engine name.
     *                 - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes (optional) Include options and/or engine_versions in the result.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters     (optional) Filters:
     *                    - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                    - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                    - engine - Filter result to only include conversions with a matching engine name.
     *                    - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes    (optional) Include options and/or engine_versions in the result.
     * @param alternative (optional) For some formats multiple alternative engines are available.
     *                    If set to true, the result includes these alternative conversion types. Default to false.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getConvertFormatsHttpUriRequest(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(alternativeToNameValuePairsConverter.convert(alternative)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT, PATH_SEGMENT_FORMATS), nameValuePairs);

        return getHttpUriRequest(HttpGet.class, uri);
    }
}
