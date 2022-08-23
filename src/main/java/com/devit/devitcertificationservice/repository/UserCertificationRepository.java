package com.devit.devitcertificationservice.repository;

import com.devit.devitcertificationservice.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {
    Optional<UserCertification> findByLoginId(String email);
}
