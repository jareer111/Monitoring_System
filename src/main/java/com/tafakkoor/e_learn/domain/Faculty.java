package com.tafakkoor.e_learn.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Faculty extends Auditable {
    @Column(nullable = false)
    private String name;

    @Builder(builderMethodName = "childBuilder")
    public Faculty(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted, String name) {
        super(id, createdAt, updatedAt, deleted);
        this.name = name;
    }

    @Override
    public String toString() {
        return "Faculty{" +
                "id='" + getId() + '\'' +
                "name='" + name + '\'' +
                "getUpdatedAt()='" + getUpdatedAt() + '\'' +
                "isDeleted()='" + isDeleted() + '\'' +
                "getCreatedAt()='" + getCreatedAt() + '\'' +
                '}';
    }
}
