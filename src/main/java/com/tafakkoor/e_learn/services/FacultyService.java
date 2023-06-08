package com.tafakkoor.e_learn.services;

import com.tafakkoor.e_learn.domain.Faculty;
import com.tafakkoor.e_learn.dto.facultyDTO.CreateFacultyDTO;
import com.tafakkoor.e_learn.dto.facultyDTO.DeleteFacultyDTO;
import com.tafakkoor.e_learn.dto.facultyDTO.UpdateFacultyDTO;
import com.tafakkoor.e_learn.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {


    private final FacultyRepository facultyRepository;

    public void create(CreateFacultyDTO faculty) {

    }

    public List<Faculty> findAll() {
        return facultyRepository.findAll();
    }

    public void update(UpdateFacultyDTO faculty) {

    }

    public void delete(DeleteFacultyDTO faculty) {

    }
}
