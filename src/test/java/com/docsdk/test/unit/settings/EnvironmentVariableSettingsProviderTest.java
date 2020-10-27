package com.docsdk.test.unit.settings;

import com.docsdk.client.setttings.AbstractSettingsProvider;
import com.docsdk.client.setttings.EnvironmentVariableSettingsProvider;
import com.docsdk.test.framework.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentVariableSettingsProviderTest {

    public static final String API_KEY = "api-key";
    public static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void success_useSandbox() {
        environmentVariables.set(AbstractSettingsProvider.API_KEY, API_KEY);
        environmentVariables.set(AbstractSettingsProvider.USE_SANDBOX, "true");
        environmentVariables.set(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final EnvironmentVariableSettingsProvider environmentVariableSettingsProvider = new EnvironmentVariableSettingsProvider();
        assertThat(environmentVariableSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(environmentVariableSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(environmentVariableSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_SANDBOX);
    }

    @Test
    public void success_useLive() {
        environmentVariables.set(AbstractSettingsProvider.API_KEY, API_KEY);
        environmentVariables.set(AbstractSettingsProvider.USE_SANDBOX, "false");
        environmentVariables.set(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final EnvironmentVariableSettingsProvider environmentVariableSettingsProvider = new EnvironmentVariableSettingsProvider();
        assertThat(environmentVariableSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(environmentVariableSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(environmentVariableSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_LIVE);
    }

    @Test
    public void failure() {
        assertThatThrownBy(EnvironmentVariableSettingsProvider::new).isInstanceOf(IllegalArgumentException.class);
    }
}
