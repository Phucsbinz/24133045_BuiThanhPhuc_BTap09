package vn.iotstar.service;

import vn.iotstar.dto.UserDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface UserService {

    UserDTO findById(Long id);

    UserDTO findByUsername(String username);

    UserDTO findByEmail(String email);

    List<UserDTO> findAll();
    Page<UserDTO> search(String keyword, int page, int size);
    UserDTO create(UserDTO dto);
    UserDTO update(Long id, UserDTO dto, Long actorId);
    void delete(Long id, Long actorId);
    long countUsers();
    long countProducts(Long userId);
}
