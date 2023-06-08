package com.tafakkoor.e_learn.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Lesson extends Auditable {
    @ManyToOne(cascade = CascadeType.MERGE)
    private Subject subject;
    @ManyToOne(cascade = CascadeType.MERGE)
    private AuthUser teacher;
    @ManyToOne(cascade = CascadeType.MERGE)
    private Group group;
    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT, pattern = "d")
    private DayOfWeek dayOfWeek;
    @Column(nullable = false, columnDefinition = "smallint")
    private Byte para;
    @ManyToOne(cascade = CascadeType.MERGE)
    private Room room;

    @Builder(builderMethodName = "childBuilder")
    public Lesson(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted, Subject subject, AuthUser teacher, Group group, DayOfWeek dayOfWeek, Byte para, Room room) {
        super(id, createdAt, updatedAt, deleted);
        this.subject = subject;
        this.teacher = teacher;
        this.group = group;
        this.dayOfWeek = dayOfWeek;
        this.para = para;
        this.room = room;
    }

    public Lesson(Long id) {
        super(id);
    }
}
