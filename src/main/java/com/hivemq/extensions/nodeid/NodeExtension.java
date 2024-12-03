package com.hivemq.extension.nodeId;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.cluster.ClusterService;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterDiscoveryInput;
import com.hivemq.extension.sdk.api.services.rest.listener.HttpListener;
import com.hivemq.extension.sdk.api.services.rest.parameter.HttpRequest;
import com.hivemq.extension.sdk.api.services.rest.parameter.HttpResponse;
import com.hivemq.extension.sdk.api.services.rest.parameter.HttpResponseImpl;

import java.util.concurrent.CompletableFuture;

public class NodeIdExtension implements ExtensionMain {

    @Override
    public void extensionStart(
            @NotNull ExtensionStartInput input,
            @NotNull ExtensionStartOutput output) {
        registerRestApi();
    }

    @Override
    public void extensionStop(
            @NotNull ExtensionStopInput input,
            @NotNull ExtensionStopOutput output) {
        // Cleanup if needed
    }

    private void registerRestApi() {
        Services.restService().addHttpListener(
            new HttpListener() {
                @Override
                public CompletableFuture<HttpResponse> onRequest(HttpRequest request) {
                    if (request.getPath().equals("/api/nodeid")) {
                        return CompletableFuture.completedFuture(getNodeId());
                    }
                    return CompletableFuture.completedFuture(new HttpResponseImpl().statusCode(404).body("Not Found"));
                }
            }
        );
    }

    private HttpResponse getNodeId() {
        // Fetch the Node ID
        ClusterService clusterService = Services.clusterService();
        ClusterDiscoveryInput clusterDiscoveryInput = clusterService.clusterDiscoveryInput();
        String nodeId = clusterDiscoveryInput.getOwnClusterId();

        // Return the Node ID in the response
        HttpResponseImpl response = new HttpResponseImpl();
        response.statusCode(200);
        response.body("Node ID: " + nodeId);
        return response;
    }
}
