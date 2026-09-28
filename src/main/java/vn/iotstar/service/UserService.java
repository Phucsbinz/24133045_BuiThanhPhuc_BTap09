package vn.iotstar.service;

import vn.iotstar.dto.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO findById(Long id);

    UserDTO findByUsername(String username);

    UserDTO findByEmail(String email);

    List<UserDTO> findAll();
}
