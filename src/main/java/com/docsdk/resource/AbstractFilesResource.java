package com.docsdk.resource;

import com.docsdk.client.mapper.ObjectMapperProvider;
import com.docsdk.client.setttings.SettingsProvider;
import com.docsdk.dto.result.AbstractResult;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractFilesResource<ISAR extends AbstractResult<InputStream>> extends AbstractResource {

    public AbstractFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Download file
     *
     * @param path path to file
     * @return IS
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ISAR download(
        @NotNull final String path
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getDownloadHttpUriRequest(
        @NotNull final String path
    ) throws URISyntaxException {
        final URI uri = new URIBuilder(path).build();

        return RequestBuilder.get().setUri(uri).build();
    }
}
