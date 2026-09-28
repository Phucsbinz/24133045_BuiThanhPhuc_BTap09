package vn.iotstar.service;

public interface OtpService {
    void issue(String email, String type);
    void deleteByEmail(String email);
}
