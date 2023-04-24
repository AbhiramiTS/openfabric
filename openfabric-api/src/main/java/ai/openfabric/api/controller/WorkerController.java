package ai.openfabric.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.github.dockerjava.api.model.Container;

import ai.openfabric.api.config.DockerClientManager;
import com.github.dockerjava.api.DockerClient;
import ai.openfabric.api.model.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import ai.openfabric.api.repository.WorkerRepository;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import ai.openfabric.api.model.statistics.WorkerStats;
import ai.openfabric.api.repository.WorkerStatsRepository;
import com.github.dockerjava.api.command.CreateContainerResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @Autowired
    private WorkerRepository workerRepository;

    @GetMapping("/listWorkers")
    public List<Worker> listWorkers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Iterable<Worker> workers = workerRepository.findAll();
        List<Worker> workerList = StreamSupport.stream(workers.spliterator(), false)
                .collect(Collectors.toList());

        int fromIndex = Math.min(page * size, workerList.size());
        int toIndex = Math.min(fromIndex + size, workerList.size());

        return workerList.subList(fromIndex, toIndex);
    }

    @PostMapping("/createWorkerContainer")
    public ResponseEntity<String> createContainer(@RequestParam("image") String imageName,
            @RequestParam("name") String name) {
        DockerClient dockerClient = DockerClientManager.getInstance();
        try {
            String containerName = "worker-" + UUID.randomUUID().toString();
            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imageName)
                    .withName(containerName)
                    .exec();

            // Store container data in the database
            Worker worker = new Worker();
            worker.setContainerId(containerResponse.getId());
            worker.setContainerName(containerName);
            worker.setImageName(imageName);
            worker.setName(name);
            worker.setContainerStatus("created");
            workerRepository.save(worker);

            return ResponseEntity.ok("Container created with ID: " + containerResponse.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating container: " + e.getMessage());
        }
    }

    @PostMapping("/startWorker/{id}")
    public String startWorker(@PathVariable String id) {
        DockerClient dockerClient = DockerClientManager.getInstance();
        try {
            StartContainerCmd startContainerCmd = dockerClient.startContainerCmd(id);
            startContainerCmd.exec();

            Worker worker = workerRepository.findByContainerId(id);
            if (worker != null) {
                worker.setContainerStatus("running");
                workerRepository.save(worker);
            }

            return "Worker container with ID: " + id + " started successfully.";
        } catch (NotFoundException e) {
            return "Worker container with ID: " + id + " not found.";
        }
    }

    @PostMapping("/stopWorker/{id}")
    public String stopWorker(@PathVariable String id) {
        DockerClient dockerClient = DockerClientManager.getInstance();
        try {
            StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(id);
            stopContainerCmd.exec();

            Worker worker = workerRepository.findByContainerId(id);
            if (worker != null) {
                worker.setContainerStatus("stopped");
                workerRepository.save(worker);
            }

            return "Worker container with ID: " + id + " stopped successfully.";
        } catch (NotFoundException e) {
            return "Worker container with ID: " + id + " not found.";
        }
    }

    @GetMapping("/getWorker/{id}")
    public Worker getWorker(@PathVariable String id) {
        return workerRepository.findById(id).orElse(null);
    }

    @Autowired
    private WorkerStatsRepository workerStatsRepository;

    @GetMapping("/getWorkerStats/{workerId}")
    public List<WorkerStats> getWorkerStats(@PathVariable String workerId) {
        return workerStatsRepository.findByWorkerId(workerId);
    }

}
