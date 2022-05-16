package testbed.repositories;

import core.repositories.ServerRepository;

import java.io.File;

public interface TestbedServerRepository extends ServerRepository {
    File getExperimentDataset();
}
