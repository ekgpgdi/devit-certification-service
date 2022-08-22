package com.devit.devitcertificationservice.sevice;

public interface VerificationCodeService {
    int makeRandomNumber();
    String send(String destination);
}
