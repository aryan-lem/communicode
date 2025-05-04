package com.communicode.communicode.repository;

import com.communicode.communicode.entity.CodeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeSnapshotRepository extends JpaRepository<CodeSnapshot, String> {
    @Query("SELECT c FROM CodeSnapshot c WHERE c.roomId = ?1 ORDER BY c.version DESC")
    Optional<CodeSnapshot> findLatestByRoomId(String roomId);
    Optional<CodeSnapshot> findTopByRoomIdOrderByVersionDesc(String roomId);
}