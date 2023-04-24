package ai.openfabric.api.repository;

import ai.openfabric.api.model.statistics.WorkerStats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerStatsRepository extends CrudRepository<WorkerStats, String> {
    List<WorkerStats> findByWorkerId(String workerId);
}
