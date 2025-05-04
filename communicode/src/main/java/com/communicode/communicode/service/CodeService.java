package com.communicode.communicode.service;

import com.communicode.communicode.entity.CodeSnapshot;
import com.communicode.communicode.exception.ResourceNotFoundException;
import com.communicode.communicode.repository.CodeSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class CodeService {

    private final CodeSnapshotRepository codeSnapshotRepository;

    @Autowired
    public CodeService(CodeSnapshotRepository codeSnapshotRepository) {
        this.codeSnapshotRepository = codeSnapshotRepository;
    }
    
    @Transactional
    public CodeSnapshot initializeCode(String roomId, String language) {
        CodeSnapshot snapshot = new CodeSnapshot();
        snapshot.setRoomId(roomId);
        snapshot.setContent("");
        snapshot.setVersion(1L);
        
        return codeSnapshotRepository.save(snapshot);
    }
    
    // @Transactional
    // public CodeSnapshot saveCode(String roomId, String content, Long version) {
    //     CodeSnapshot latestSnapshot = codeSnapshotRepository.findLatestByRoomId(roomId)
    //         .orElseThrow(() -> new ResourceNotFoundException("No code found for this room"));
            
    //     // Only save if version is newer
    //     if (version > latestSnapshot.getVersion()) {
    //         CodeSnapshot snapshot = new CodeSnapshot();
    //         snapshot.setRoomId(roomId);
    //         snapshot.setContent(content);
    //         snapshot.setVersion(version);
            
    //         return codeSnapshotRepository.save(snapshot);
    //     }
        
    //     return latestSnapshot;
    // }
        @Transactional
    public void saveCode(String roomId, String content, long version) {
        try {
            // Use findTopByRoomIdOrderByVersionDesc instead of findLatestByRoomId
            CodeSnapshot snapshot = new CodeSnapshot();
            snapshot.setRoomId(roomId);
            snapshot.setContent(content);
            snapshot.setVersion(version);
            snapshot.setSavedAt(LocalDateTime.now());
            
            codeSnapshotRepository.save(snapshot);
        } catch (Exception e) {
            // logger.error("Error saving code snapshot: {}", e.getMessage(), e);
            System.out.println(e.getMessage());
        }
    }
    public CodeSnapshot getLatestSnapshot(String roomId) {
        return codeSnapshotRepository.findTopByRoomIdOrderByVersionDesc(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("No code found for this room"));
    }
    
    // Optionally, add the helper method you were trying to use:
    public String getLatestCodeContent(String roomId) {
        try {
            CodeSnapshot snapshot = getLatestSnapshot(roomId);
            return snapshot != null ? snapshot.getContent() : "";
        } catch (ResourceNotFoundException e) {
            return ""; // Return empty string if no snapshot exists
        }
    }
}