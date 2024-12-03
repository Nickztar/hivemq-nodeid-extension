/*
 * Copyright 2018-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.nodeid;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This tests the functionality of the {@link HelloWorldInterceptor}.
 * It uses the HiveMQ Testcontainer to automatically package and deploy this
 * extension inside a HiveMQ docker container.
 * <p>
 * This integration test MUST be executed by Gradle as the extension is built by
 * the 'hivemqExtensionZip' task.
 *
 * @author Yannick Weber
 * @since 4.3.1
 */
@Testcontainers
class HelloWorldInterceptorIT {

    @Container
    final @NotNull HiveMQContainer extension = new HiveMQContainer(
            DockerImageName.parse("hivemq/hivemq4").withTag("dns-latest"))
            .withHiveMQConfig(MountableFile.forClasspathResource("config.xml"))
            .withExtension(MountableFile.forClasspathResource("hivemq-nodeid-extension"));

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void test_payload_modified() throws InterruptedException, IOException {
        // Path to the file containing the Node ID
        final Path nodeIdFilePath = Paths.get("/opt/hivemq/node-id.txt");

        // // Read the Node ID from the file
        String nodeId = "123";
        // try {
        // nodeId = Files.readString(nodeIdFilePath).trim();
        // System.out.println("Node ID read from file: " + nodeId);
        // } catch (IOException e) {
        // throw new IOException("Failed to read Node ID from file: " + nodeIdFilePath,
        // e);
        // }

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier("hello-world-client")
                .serverPort(extension.getMqttPort())
                .buildBlocking();
        client.connect();

        final Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL);
        client.subscribeWith().topicFilter("hello/world").send();

        // Publish a message with a payload containing the Node ID
        final String messagePayload = "Good Bye World! Node ID: " + nodeId;
        client.publishWith().topic("hello/world").payload(messagePayload.getBytes(StandardCharsets.UTF_8)).send();

        // Receive the published message
        final Mqtt5Publish received = publishes.receive();

        // Extract the payload and validate it contains the expected Node ID
        final String receivedPayload = new String(received.getPayloadAsBytes(), StandardCharsets.UTF_8);
        assertTrue(receivedPayload.contains("Node ID: " + nodeId),
                "Received payload does not contain the expected Node ID.");
        publishes.close();

        String nodeIdFilePathInContainer = "/opt/hivemq/node-id.txt";
        String nodeIdContent = extension.copyFileFromContainer(nodeIdFilePathInContainer, inputStream -> {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }).trim();
        assertNotNull(nodeIdContent);
        System.out.println("Node ID read from file: " + nodeIdFilePathInContainer);
    }

}