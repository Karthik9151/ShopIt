package com.shopit.repository;

import com.shopit.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    List<Coupon> findAllByOrderByCreatedAtDesc();

    Page<Coupon> findByActiveFalse(Pageable pageable);

    @Query("select c from Coupon c where c.active = true and c.startsAt > :now")
    Page<Coupon> findScheduled(@Param("now") Instant now, Pageable pageable);

    @Query("""
        select c from Coupon c
        where c.active = true
          and (c.endsAt <= :now or (c.usageLimit is not null and c.usageCount >= c.usageLimit))
        """)
    Page<Coupon> findExpired(@Param("now") Instant now, Pageable pageable);

    @Query("""
        select c from Coupon c
        where c.active = true
          and c.startsAt <= :now
          and c.endsAt > :now
          and (c.usageLimit is null or c.usageCount < c.usageLimit)
        """)
    Page<Coupon> findCurrentlyActive(@Param("now") Instant now, Pageable pageable);
    List<Coupon> findByActiveTrueOrderByEndsAtAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where upper(c.code) = upper(:code)")
    Optional<Coupon> findByCodeIgnoreCaseForUpdate(@Param("code") String code);
}
