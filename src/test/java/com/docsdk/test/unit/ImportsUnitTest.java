package com.docsdk.test.unit;

import com.docsdk.client.DocSDKClient;
import com.docsdk.client.mapper.ObjectMapperProvider;
import com.docsdk.client.setttings.SettingsProvider;
import com.docsdk.dto.request.AzureBlobImportRequest;
import com.docsdk.dto.request.GoogleCloudStorageImportRequest;
import com.docsdk.dto.request.OpenStackImportRequest;
import com.docsdk.dto.request.S3ImportRequest;
import com.docsdk.dto.request.SftpImportRequest;
import com.docsdk.dto.request.UploadImportRequest;
import com.docsdk.dto.request.UrlImportRequest;
import com.docsdk.dto.response.TaskResponse;
import com.docsdk.dto.result.Result;
import com.docsdk.executor.RequestExecutor;
import com.docsdk.resource.AbstractResource;
import com.docsdk.test.framework.AbstractTest;
import com.docsdk.test.framework.UnitTest;
import com.google.common.collect.ImmutableMap;
import com.pivovarit.function.ThrowingSupplier;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class ImportsUnitTest extends AbstractTest {

    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private RequestExecutor requestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    @Mock
    private InputStream inputStream;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestArgumentCaptor;

    private DocSDKClient docSDKClient;

    @Before
    public void before() {
        when(settingsProvider.getApiKey()).thenReturn(API_KEY);
        when(settingsProvider.getApiUrl()).thenReturn(API_URL);

        docSDKClient = new DocSDKClient(settingsProvider, objectMapperProvider, requestExecutor);
    }

    @Test
    public void import_url() throws Exception {
        final UrlImportRequest expectedUrlImportRequest = new UrlImportRequest().setFilename("import-url-filename");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().url(expectedUrlImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/url");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final UrlImportRequest actualUrlImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), UrlImportRequest.class)).get();

            assertThat(actualUrlImportRequest.getFilename()).isEqualTo(expectedUrlImportRequest.getFilename());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_upload_noImmediateUpload() throws Exception {
        final UploadImportRequest expectedUploadImportRequest = new UploadImportRequest().setRedirect("import-upload-redirect");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().upload(expectedUploadImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/upload");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final UploadImportRequest actualUploadImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), UploadImportRequest.class)).get();

            assertThat(actualUploadImportRequest.getRedirect()).isEqualTo(expectedUploadImportRequest.getRedirect());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_upload_immediateUpload() throws Exception {
        final UploadImportRequest expectedUploadImportRequest = new UploadImportRequest().setRedirect("import-upload-redirect");
        final Map<String, String> parameters = ImmutableMap.of("expires", "expires", "max-file-count", "max-file-count",
            "max-file-size", "max-file-size", "redirect", "redirect", "signature", "signature");
        final TaskResponse taskResponse = new TaskResponse().setId("import-upload-task-id").setResult(
            new TaskResponse.Result().setForm(new TaskResponse.Result.Form().setUrl("import-upload-task-result-form-url").setParameters(parameters)));
        final Result<TaskResponse> showTaskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE)))
            .thenReturn(Result.<TaskResponse>builder().status(HttpStatus.SC_CREATED).body(taskResponse).build()).thenReturn(showTaskResponseResult);
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE)))
            .thenReturn(Result.<Void>builder().status(HttpStatus.SC_CREATED).build());

        assertThat(docSDKClient.importUsing().upload(expectedUploadImportRequest, inputStream)).isEqualTo(showTaskResponseResult);
        verify(requestExecutor, times(2)).execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(taskResponse.getResult().getForm().getUrl());
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_s3() throws Exception {
        final S3ImportRequest expectedS3ImportRequest = new S3ImportRequest().setBucket("import-s3-bucket");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().s3(expectedS3ImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/s3");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final S3ImportRequest actualS3ImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), S3ImportRequest.class)).get();

            assertThat(actualS3ImportRequest.getBucket()).isEqualTo(expectedS3ImportRequest.getBucket());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_azureBlob() throws Exception {
        final AzureBlobImportRequest expectedAzureBlobImportRequest = new AzureBlobImportRequest().setStorageAccount("import-azure-blob-storage-account");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().azureBlob(expectedAzureBlobImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/azure/blob");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final AzureBlobImportRequest actualAzureBlobImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), AzureBlobImportRequest.class)).get();

            assertThat(actualAzureBlobImportRequest.getStorageAccount()).isEqualTo(expectedAzureBlobImportRequest.getStorageAccount());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_googleCloudStorage() throws Exception {
        final GoogleCloudStorageImportRequest expectedGoogleCloudStorageImportRequest = new GoogleCloudStorageImportRequest().setBucket("import-google-cloud-storage-bucket");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().googleCloudStorage(expectedGoogleCloudStorageImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/google-cloud-storage");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final GoogleCloudStorageImportRequest actualGoogleCloudStorageImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), GoogleCloudStorageImportRequest.class)).get();

            assertThat(actualGoogleCloudStorageImportRequest.getBucket()).isEqualTo(expectedGoogleCloudStorageImportRequest.getBucket());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_openStack() throws Exception {
        final OpenStackImportRequest expectedOpenStackImportRequest = new OpenStackImportRequest().setContainer("import-open-stack-container");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().openStack(expectedOpenStackImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/openstack");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final OpenStackImportRequest actualOpenStackImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), OpenStackImportRequest.class)).get();

            assertThat(actualOpenStackImportRequest.getContainer()).isEqualTo(expectedOpenStackImportRequest.getContainer());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_sftp() throws Exception {
        final SftpImportRequest expectedSftpImportRequest = new SftpImportRequest().setFilename("import-sftp-filename");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(docSDKClient.importUsing().sftp(expectedSftpImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/sftp");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final SftpImportRequest actualSftpImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), SftpImportRequest.class)).get();

            assertThat(actualSftpImportRequest.getFilename()).isEqualTo(actualSftpImportRequest.getFilename());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @After
    public void after() throws Exception {
        docSDKClient.close();
    }
}
