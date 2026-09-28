package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ảnh.");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Ảnh tối đa 5 MB.");
        String contentType = file.getContentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType) && !"image/png".equalsIgnoreCase(contentType))
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPEG hoặc PNG.");
        try {
            byte[] bytes = file.getBytes();
            var decoded = ImageIO.read(new ByteArrayInputStream(bytes));
            if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0)
                throw new IllegalArgumentException("Nội dung file không phải ảnh hợp lệ.");
            Map<?, ?> result = cloudinary.uploader().upload(bytes,
                    Map.of("folder", "btap09/products", "resource_type", "image"));
            return new CloudinaryUploadResult(String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id")));
        } catch (IOException e) {
            throw new IllegalArgumentException("Không đọc được file ảnh.", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải ảnh lên Cloudinary.", e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
        } catch (Exception e) {
            throw new IllegalStateException("Không thể xóa ảnh trên Cloudinary.", e);
        }
    }
}
