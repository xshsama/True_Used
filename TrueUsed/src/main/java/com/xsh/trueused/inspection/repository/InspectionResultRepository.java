package com.xsh.trueused.inspection.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xsh.trueused.entity.InspectionResult;

@Repository
public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {
    @EntityGraph(attributePaths = "item")
    List<InspectionResult> findByInspectionId(Long inspectionId);
}
