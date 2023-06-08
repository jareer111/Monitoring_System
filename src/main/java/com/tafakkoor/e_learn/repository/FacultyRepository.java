package com.tafakkoor.e_learn.repository;

import com.tafakkoor.e_learn.domain.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {
    @Transactional
    @Modifying
    @Query("update Faculty f set f.name = ?2 , f.updatedAt = ?3 where f.id = ?1")
    void update(Integer id, String name, LocalDateTime updatedAt);
    @Transactional
    @Modifying
    @Query("update Faculty f set f.deleted = true, f.updatedAt = ?2 where f.id = ?1")
    void delete(Integer id,LocalDateTime updatedAt);

    @Query("select f from Faculty f where f.deleted = false")
    Optional<Faculty> findByDeletedFalse(Faculty faculty);


}
