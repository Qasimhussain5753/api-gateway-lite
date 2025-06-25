package model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class dynamic_routes_model {
    @Id @GeneratedValue
    public Long id;
    public String path;
    public String method;
    public String targetUrl;
    public boolean enabled = true; 
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime ttl;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }   
}
