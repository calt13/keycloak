/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.it.cli.dist;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.keycloak.config.CachingOptions;
import org.keycloak.config.Option;
import org.keycloak.it.junit5.extension.DistributionTest;
import org.keycloak.it.junit5.extension.DryRun;
import org.keycloak.it.junit5.extension.RawDistOnly;
import org.keycloak.it.utils.KeycloakDistribution;

@DistributionTest
public class CacheEmbeddedMtlsDistTest {

    @DryRun
    @Test
    @RawDistOnly(reason = "Containers are immutable")
    public void testCacheEmbeddedMtlsDisabled(KeycloakDistribution dist) {
        for (var option : Arrays.asList(
                CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE,
                CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE,
                CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE_PASSWORD,
                CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE_PASSWORD
        )) {
            var result = dist.run("start-dev", "--cache=ispn", "--%s=a".formatted(option.getKey()));
            result.assertError("Disabled option: '--%s'. Available only when property 'cache-embedded-mtls-enabled' is enabled.".formatted(option.getKey()));
        }
    }

    @DryRun
    @Test
    @RawDistOnly(reason = "Containers are immutable")
    public void testCacheEmbeddedMtlsFileValidation(KeycloakDistribution dist) {
        doFileAndPasswordValidation(dist, CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE, CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE_PASSWORD);
        doFileAndPasswordValidation(dist, CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE, CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE_PASSWORD);
    }

    @Test
    @RawDistOnly(reason = "Containers are immutable")
    public void testCacheEmbeddedMtlsEnabled(KeycloakDistribution dist) {
        var result = dist.run("start-dev", "--cache=ispn", "--cache-embedded-mtls-enabled=true");
        result.assertMessage("JGroups JDBC_PING discovery enabled.");
        result.assertMessage("JGroups Encryption enabled (mTLS).");
    }

    private void doFileAndPasswordValidation(KeycloakDistribution dist, Option<String> fileOption, Option<String> passwordOption) {
        var result = dist.run("start-dev", "--cache=ispn", "--cache-embedded-mtls-enabled=true", "--%s=file".formatted(fileOption.getKey()));
        result.assertError("The option '%s' requires '%s' to be enabled.".formatted(fileOption.getKey(), passwordOption.getKey()));

        result = dist.run("start-dev", "--cache=ispn", "--cache-embedded-mtls-enabled=true", "--%s=secret".formatted(passwordOption.getKey()));
        result.assertError("The option '%s' requires '%s' to be enabled.".formatted(passwordOption.getKey(), fileOption.getKey()));
    }


}
