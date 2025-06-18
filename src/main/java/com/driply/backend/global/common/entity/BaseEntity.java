package com.driply.backend.global.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@ToString(of = {"createdAt", "updatedAt", "deleted"})
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

/*
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }
*/

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted && this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        } else if (!this.deleted && this.deletedAt != null) {
            this.deletedAt = null;
        }
    }

    public void softDelete() {
        if (!deleted) {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
        }
    }

    public void undoDelete() {
        if (deleted) {
            this.deleted = false;
            this.deletedAt = null;
        }
    }

    public boolean isActive() {
        return !this.deleted;
    }

    public boolean isDeletedStateValid() {
        return (deleted && deletedAt != null) || (!deleted && deletedAt == null);
    }
}
