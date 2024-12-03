package com.hivemq.extensions.nodeid;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.cluster.ClusterDiscoveryCallback;
import com.hivemq.extension.sdk.api.services.cluster.ClusterService;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterDiscoveryInput;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterDiscoveryOutput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NodeIdExtension implements ExtensionMain {

    private static final String NODE_ID_FILE_PATH = "/opt/hivemq/node-id.txt"; // Adjust path as needed

    @Override
    public void extensionStart(
            @NotNull ExtensionStartInput input,
            @NotNull ExtensionStartOutput output) {
        registerDiscoveryCallback();
    }

    @Override
    public void extensionStop(
            @NotNull ExtensionStopInput input,
            @NotNull ExtensionStopOutput output) {
        // Cleanup logic if needed
    }

    private void registerDiscoveryCallback() {
        ClusterService clusterService = Services.clusterService();

        clusterService.addDiscoveryCallback(new ClusterDiscoveryCallback() {
            @Override
            public void reload(@NotNull ClusterDiscoveryInput discoveryInput,
                    @NotNull ClusterDiscoveryOutput discoveryOutput) {
                // Retrieve the current node's ID
                String nodeId = discoveryInput.getOwnClusterId();

                // Write the Node ID to a file
                writeNodeIdToFile(nodeId);
            }

            @Override
            public void init(@NotNull ClusterDiscoveryInput discoveryInput,
                    @NotNull ClusterDiscoveryOutput discoveryOutput) {
                // Retrieve the current node's ID
                String nodeId = discoveryInput.getOwnClusterId();
                // Write the Node ID to a file
                writeNodeIdToFile(nodeId);
            }

            @Override
            public void destroy(@NotNull ClusterDiscoveryInput clusterDiscoveryInput) {
            }
        });
    }

    private void writeNodeIdToFile(String nodeId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NODE_ID_FILE_PATH))) {
            writer.write(nodeId);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write Node ID to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
