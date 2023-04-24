package ai.openfabric.api.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerClientManager {
    private static DockerClient dockerClient;

    public static DockerClient getInstance() {
        if (dockerClient == null) {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://localhost:2375")
                    .build();

            dockerClient = DockerClientBuilder.getInstance(config).build();
        }
        return dockerClient;
    }
}
