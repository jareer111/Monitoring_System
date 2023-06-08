package com.tafakkoor.e_learn.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group extends Auditable {
    @Column(nullable = false)
    private String name;
    @ManyToOne
    private Faculty faculty;
    @Column(nullable = false, columnDefinition = "smallint default 1")
    private Byte course;

    @ManyToOne
    private AuthUser owner;

    @Builder(builderMethodName = "childBuilder")
    public Group(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted, String name, Faculty faculty, Byte course, AuthUser owner) {
        super(id, createdAt, updatedAt, deleted);
        this.name = name;
        this.faculty = faculty;
        this.course = course;
        this.owner = owner;
    }
}
