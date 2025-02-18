package org.keycloak.testsuite.util.oauth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.keycloak.OAuth2Constants;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHttpResponse {

    private final CloseableHttpResponse response;
    private final int statusCode;
    private final Map<String, String> headers;

    private String error;
    private String errorDescription;

    public AbstractHttpResponse(CloseableHttpResponse response) throws IOException {
        try (response) {
            this.response = response;
            this.statusCode = response.getStatusLine().getStatusCode();

            this.headers = new HashMap<>();
            for (Header h : response.getAllHeaders()) {
                headers.put(h.getName(), h.getValue());
            }

            if (isSuccess()) {
                parseContent();
            } else if (isJson()) {
                parseError();
            }
        }
    }

    protected int getSuccessCode() {
        return 200;
    }

    public boolean isSuccess() {
        return statusCode == getSuccessCode();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    protected String getContentType() {
        Header[] contentTypeHeaders = response.getHeaders("Content-Type");
        return contentTypeHeaders != null && contentTypeHeaders.length > 0 ? contentTypeHeaders[0].getValue() : null;
    }

    protected ObjectNode asJson() throws IOException {
        return asJson(ObjectNode.class);
    }

    protected <S> S asJson(Class<S> clazz) throws IOException {
        assertJsonContentType();
        return JsonSerialization.readValue(response.getEntity().getContent(), clazz);
    }

    protected abstract void parseContent() throws IOException;

    private void parseError() throws IOException {
        if (getStatusCode() == 504) {
            return;
        }

        ObjectNode json = asJson(ObjectNode.class);
        error = json.get(OAuth2Constants.ERROR).asText();
        errorDescription = json.has(OAuth2Constants.ERROR_DESCRIPTION) ? json.get(OAuth2Constants.ERROR_DESCRIPTION).asText() : null;
    }

    protected boolean isJson() {
        return "application/json".equals(getContentType());
    }

    protected void assertJsonContentType() {
        String contentType = getContentType();
        if (contentType == null || !contentType.startsWith("application/json")) {
            Assert.fail("Invalid content type. Status: " + getStatusCode() + ", contentType: " + contentType);
        }
    }

}
