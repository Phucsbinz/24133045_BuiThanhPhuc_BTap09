package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;

public interface ProductService {
    Page<ProductDTO> findVisible(String keyword, int page, int size, Long userId, boolean admin);
    ProductDTO findVisibleById(Long id, Long userId, boolean admin);
    ProductDTO create(ProductDTO dto, MultipartFile image, Long ownerId);
    ProductDTO update(Long id, ProductDTO dto, MultipartFile image, Long userId, boolean admin);
    void delete(Long id, Long userId, boolean admin);
    long countAll();
    long countByUser(Long userId);
}
