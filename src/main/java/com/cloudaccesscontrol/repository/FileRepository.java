package com.cloudaccesscontrol.repository;

import com.cloudaccesscontrol.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findByUsername(String username);
}