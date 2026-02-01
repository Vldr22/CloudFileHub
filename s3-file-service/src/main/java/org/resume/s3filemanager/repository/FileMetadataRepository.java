package org.resume.s3filemanager.repository;

import jakarta.transaction.Transactional;
import org.resume.common.model.ScanStatus;
import org.resume.s3filemanager.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    boolean existsByFileHashAndUserId(String fileHash, Long userId);

    Optional<FileMetadata> findByUniqueName(String uniqueName);

    @Modifying
    @Transactional
    @Query("DELETE FROM FileMetadata f WHERE f.uniqueName = :uniqueName")
    int deleteByUniqueName(@Param("uniqueName") String uniqueName);

    Page<FileMetadata> findByScanStatus(ScanStatus scanStatus, Pageable pageable);

    List<FileMetadata> findAllByScanStatus(ScanStatus scanStatus);

    long countByScanStatus(ScanStatus scanStatus);
}
