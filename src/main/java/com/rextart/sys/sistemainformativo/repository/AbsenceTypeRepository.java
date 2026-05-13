package com.rextart.sys.sistemainformativo.repository;

import com.rextart.sys.sistemainformativo.model.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbsenceTypeRepository extends JpaRepository<AbsenceType, Long> {

    boolean existsByCode(String code);

    List<AbsenceType> findAllByOrderByCodeAsc();
}