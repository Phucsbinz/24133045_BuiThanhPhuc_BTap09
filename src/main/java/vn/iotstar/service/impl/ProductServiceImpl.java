package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.ProductService;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository products;
    private final UserRepository users;
    private final ProductMapper mapper;
    private final CloudinaryService cloudinary;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findVisible(String keyword, int page, int size, Long userId, boolean admin) {
        return products.searchVisible(keyword == null ? "" : keyword.trim(), userId, admin,
                        PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50),
                                Sort.by(Sort.Direction.DESC, "id")))
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findVisibleById(Long id, Long userId, boolean admin) {
        return products.findVisibleById(id, userId, admin).map(mapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm hoặc bạn không có quyền xem."));
    }

    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile image, Long ownerId) {
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản người bán."));
        Product product = mapper.toEntity(dto);
        product.setUser(owner);
        CloudinaryUploadResult uploaded = uploadIfPresent(image);
        if (uploaded != null) {
            product.setImageUrl(uploaded.url());
            product.setCloudinaryPublicId(uploaded.publicId());
            cleanupIfTransactionRollsBack(uploaded.publicId());
        }
        return mapper.toDto(products.save(product));
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile image, Long userId, boolean admin) {
        Product product = visibleForMutation(id, userId, admin);
        String oldPublicId = product.getCloudinaryPublicId();
        CloudinaryUploadResult uploaded = uploadIfPresent(image);
        mapper.updateEntity(dto, product);
        if (uploaded != null) {
            product.setImageUrl(uploaded.url());
            product.setCloudinaryPublicId(uploaded.publicId());
            cleanupIfTransactionRollsBack(uploaded.publicId());
        }
        Product saved = products.save(product);
        if (uploaded != null && oldPublicId != null && !oldPublicId.isBlank())
            cleanupAfterCommit(oldPublicId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId, boolean admin) {
        Product product = visibleForMutation(id, userId, admin);
        String publicId = product.getCloudinaryPublicId();
        products.delete(product);
        if (publicId != null && !publicId.isBlank()) cleanupAfterCommit(publicId);
    }

    private Product visibleForMutation(Long id, Long userId, boolean admin) {
        return products.findVisibleById(id, userId, admin)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm hoặc bạn không có quyền thao tác."));
    }

    private CloudinaryUploadResult uploadIfPresent(MultipartFile image) {
        return image == null || image.isEmpty() ? null : cloudinary.upload(image);
    }

    private void cleanupAfterCommit(String publicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try { cloudinary.delete(publicId); }
                catch (RuntimeException e) { log.error("Cloudinary cleanup failed; retry publicId={}", publicId, e); }
            }
        });
    }

    private void cleanupIfTransactionRollsBack(String publicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try { cloudinary.delete(publicId); }
                    catch (RuntimeException e) { log.error("Cloudinary compensation failed; retry publicId={}", publicId, e); }
                }
            }
        });
    }

    @Override @Transactional(readOnly = true)
    public long countAll() { return products.count(); }

    @Override @Transactional(readOnly = true)
    public long countByUser(Long userId) { return products.countByUser_Id(userId); }
}
