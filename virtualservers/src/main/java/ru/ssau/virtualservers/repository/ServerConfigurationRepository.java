package ru.ssau.virtualservers.repository;

import ru.ssau.virtualservers.entity.ServerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerConfigurationRepository extends JpaRepository<ServerConfiguration, Long> {}