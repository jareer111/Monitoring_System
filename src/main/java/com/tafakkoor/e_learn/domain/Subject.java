package com.tafakkoor.e_learn.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Subject extends Auditable {
    @Column(nullable = false)
    private String name;

    @Builder(builderMethodName = "childBuilder")
    public Subject(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted, String name) {
        super(id, createdAt, updatedAt, deleted);
        this.name = name;
    }
}
