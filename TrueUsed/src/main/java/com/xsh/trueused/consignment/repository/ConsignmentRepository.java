package com.xsh.trueused.consignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xsh.trueused.entity.Consignment;
import com.xsh.trueused.enums.ConsignmentStatus;

@Repository
public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    List<Consignment> findBySellerId(Long sellerId);

    List<Consignment> findByStatus(ConsignmentStatus status);

    @Query("select c.product.id from Consignment c where c.id = :consignmentId")
    Optional<Long> findProductIdById(@Param("consignmentId") Long consignmentId);
}
