package com.communicode.communicode.repository;

import com.communicode.communicode.entity.EditorRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EditorRoomRepository extends JpaRepository<EditorRoom, String> {
    @Query("SELECT r FROM EditorRoom r WHERE r.expiresAt < ?1")
    List<EditorRoom> findRoomsExpiredBefore(LocalDateTime dateTime);
    
    boolean existsById(String id);
}