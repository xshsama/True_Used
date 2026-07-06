package com.xsh.trueused.product.service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsh.trueused.product.dto.ProductCreateRequest;
import com.xsh.trueused.product.dto.ProductDTO;
import com.xsh.trueused.product.dto.ProductUpdateRequest;
import com.xsh.trueused.entity.Category;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.ProductImage;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.entity.UserCoupon;
import com.xsh.trueused.enums.CouponType;
import com.xsh.trueused.enums.ProductCondition;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.product.mapper.ProductMapper;
import com.xsh.trueused.category.repository.CategoryRepository;
import com.xsh.trueused.interaction.repository.FavoriteRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.coupon.repository.UserCouponRepository;
import com.xsh.trueused.user.repository.UserRepository;
import com.xsh.trueused.notification.service.NotificationService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserCouponRepository userCouponRepository;

    private static final String KEY_STATIC = "product:static:";
    private static final String KEY_STATUS = "product:status:";
    private static final String KEY_VIEWS = "product:views:";
    private static final String KEY_FAVS = "product:favs:";

    @Transactional
    public ProductDTO create(ProductCreateRequest req, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Product p = new Product();
        p.setSeller(seller);
        applyCreate(req, p);

        // 根据交易模式设置初始状态
        if (p.getTradeModel() == com.xsh.trueused.enums.ProductTradeModel.FREE_TRADING) {
            p.setStatus(ProductStatus.ON_SALE); // 自主售卖直接上架
        } else {
            p.setStatus(ProductStatus.PENDING); // 其他模式（如寄售）默认为待入仓/待处理
        }

        Product saved = productRepository.save(p);
        return ProductMapper.enrich(ProductMapper.toDTO(saved));
    }

    @Transactional
    public ProductDTO update(Long id, ProductUpdateRequest req, Long sellerId) {
        return update(id, req, sellerId, false);
    }

    @Transactional
    public ProductDTO update(Long id, ProductUpdateRequest req, Long sellerId, boolean admin) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        assertCanManageProduct(p, sellerId, admin, "无权修改");

        BigDecimal oldPrice = p.getPrice();
        ProductStatus oldStatus = p.getStatus();

        applyUpdate(req, p, admin);

        // Price Drop Notification
        if (req.price() != null && req.price().compareTo(oldPrice) < 0) {
            notifyPriceDrop(p, oldPrice, req.price());
        }

        if (p.getStatus() != oldStatus) {
            invalidateProductStatusCache(id, p.getStatus());
        } else {
            evictProductStaticCache(id);
        }

        return ProductMapper.enrich(ProductMapper.toDTO(p));
    }

    private void notifyPriceDrop(Product p, BigDecimal oldPrice, BigDecimal newPrice) {
        java.util.List<com.xsh.trueused.entity.Favorite> favorites = favoriteRepository.findByProduct(p);
        for (com.xsh.trueused.entity.Favorite fav : favorites) {
            String title = "降价提醒";
            String content = String.format("您收藏的宝贝“%s”降价了！从 ¥%s 降至 ¥%s",
                    p.getTitle(), oldPrice, newPrice);
            notificationService.createNotification(
                    fav.getUser().getId(),
                    title,
                    content,
                    "PRICE_DROP",
                    p.getId());
        }
    }

    @Transactional
    public void polishProduct(Long id, Long sellerId) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (!Objects.equals(p.getSeller().getId(), sellerId)) {
            throw new SecurityException("无权操作");
        }

        // Check if already polished today
        java.time.Instant lastPolished = p.getLastPolishedAt();
        java.time.LocalDate today = java.time.LocalDate.now();

        if (lastPolished != null) {
            java.time.LocalDate lastPolishedDate = java.time.LocalDate.ofInstant(lastPolished,
                    java.time.ZoneId.systemDefault());
            if (lastPolishedDate.equals(today)) {
                throw new IllegalStateException("每天只能擦亮一次");
            }
        }

        // Check and consume promotion coupon
        java.util.List<UserCoupon> coupons = userCouponRepository.findByUserIdAndCoupon_TypeAndIsUsedFalse(sellerId,
                CouponType.PROMOTION);
        UserCoupon validCoupon = null;
        java.time.Instant now = java.time.Instant.now();
        for (UserCoupon coupon : coupons) {
            if (coupon.getValidUntil() == null || coupon.getValidUntil().isAfter(now)) {
                validCoupon = coupon;
                break;
            }
        }

        if (validCoupon == null) {
            throw new IllegalArgumentException("需要推广券才能擦亮商品");
        }

        validCoupon.setIsUsed(true);
        validCoupon.setUsedAt(now);
        userCouponRepository.save(validCoupon);

        p.setLastPolishedAt(now);
        p.setUpdatedAt(now); // Bump to top
        productRepository.save(p);

        // Invalidate cache
        redisTemplate.delete(KEY_STATIC + id);
    }

    @Transactional
    public void delete(Long id, Long sellerId) {
        delete(id, sellerId, false);
    }

    @Transactional
    public void delete(Long id, Long sellerId, boolean admin) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        assertCanManageProduct(p, sellerId, admin, "无权删除");
        productRepository.delete(p);

        // Clean up cache
        redisTemplate.delete(KEY_STATIC + id);
        redisTemplate.delete(KEY_STATUS + id);
        redisTemplate.delete(KEY_VIEWS + id);
        redisTemplate.delete(KEY_FAVS + id);
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOne(Long id) {
        ProductDTO staticDto = null;
        ProductStatus dbStatus = null;
        String staticKey = KEY_STATIC + id;

        // 1. Try fetch static info from Redis
        try {
            String json = redisTemplate.opsForValue().get(staticKey);
            if (json != null) {
                staticDto = objectMapper.readValue(json, ProductDTO.class);
            }
        } catch (Exception e) {
            log.error("Failed to read product static cache", e);
        }

        // 2. If missing, fetch from DB
        if (staticDto == null) {
            Optional<Product> pOpt = productRepository.findById(id);
            if (pOpt.isEmpty()) {
                return Optional.empty();
            }
            Product product = pOpt.get();
            staticDto = ProductMapper.toDTO(product);
            dbStatus = product.getStatus();
            // Save to Redis (TTL 6h)
            try {
                redisTemplate.opsForValue().set(staticKey, objectMapper.writeValueAsString(staticDto), 6,
                        TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Failed to write product static cache", e);
            }
        }

        if (dbStatus == null) {
            Optional<ProductStatus> statusOpt = productRepository.findStatusById(id);
            if (statusOpt.isEmpty()) {
                return Optional.empty();
            }
            dbStatus = statusOpt.get();
        }

        // 3. Fetch dynamic data (Status, Views, Favorites)
        ProductStatus status = dbStatus;
        Long views = staticDto.viewsCount();
        Long favs = staticDto.favoritesCount();

        try {
            redisTemplate.opsForValue().set(KEY_STATUS + id, status.name());
        } catch (Exception e) {
            log.warn("Failed to sync product status cache for product {}", id, e);
        }

        // Views
        String viewsStr = redisTemplate.opsForValue().get(KEY_VIEWS + id);
        if (viewsStr != null) {
            views = Long.parseLong(viewsStr);
        } else {
            if (views == null)
                views = 0L;
            redisTemplate.opsForValue().set(KEY_VIEWS + id, String.valueOf(views));
        }

        // Favs
        String favsStr = redisTemplate.opsForValue().get(KEY_FAVS + id);
        if (favsStr != null) {
            favs = Long.parseLong(favsStr);
        } else {
            if (favs == null)
                favs = 0L;
            redisTemplate.opsForValue().set(KEY_FAVS + id, String.valueOf(favs));
        }

        // 4. Merge and return
        return Optional.of(ProductMapper.enrich(withDynamicData(staticDto, status, views, favs)));
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOneForViewer(Long id, Long viewerId, boolean admin) {
        return findOne(id).filter(dto -> canViewProductDetail(dto, viewerId, admin));
    }

    private boolean canViewProductDetail(ProductDTO dto, Long viewerId, boolean admin) {
        if (dto.status() == ProductStatus.ON_SALE) {
            return true;
        }
        return admin || isSeller(dto, viewerId);
    }

    private boolean isSeller(ProductDTO dto, Long viewerId) {
        return viewerId != null && dto.seller() != null && Objects.equals(dto.seller().id(), viewerId);
    }

    private ProductDTO withDynamicData(ProductDTO dto, ProductStatus status, Long views, Long favs) {
        return new ProductDTO(
                dto.id(), dto.title(), dto.description(), dto.price(), dto.originalPrice(), dto.heatScore(),
                dto.currency(), status, dto.condition(), dto.sellerClaimCondition(), dto.inspectionGrade(),
                dto.tradeModel(), dto.seller(), dto.category(),
                dto.locationText(), dto.lat(), dto.lng(), views, favs, dto.images(), dto.createdAt(), dto.updatedAt());
    }

    @Transactional
    public void incrementViews(Long id) {
        redisTemplate.opsForValue().increment(KEY_VIEWS + id);
        redisTemplate.opsForSet().add("product:views:dirty", String.valueOf(id));
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Sync every minute
    @Transactional
    public void syncViewsToDB() {
        java.util.List<String> dirtyIds = redisTemplate.opsForSet().pop("product:views:dirty", 100);
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return;
        }

        for (String idStr : dirtyIds) {
            try {
                Long id = Long.valueOf(idStr);
                String viewsStr = redisTemplate.opsForValue().get(KEY_VIEWS + id);
                if (viewsStr != null) {
                    Long views = Long.valueOf(viewsStr);
                    productRepository.updateViewsCount(id, views);
                }
            } catch (Exception e) {
                log.error("Failed to sync views for product " + idStr, e);
            }
        }
    }

    @Transactional
    public void updateProductStatus(Long id, ProductStatus status) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        p.setStatus(status);
        productRepository.save(p);

        invalidateProductStatusCache(id, status);
    }

    @Transactional
    public boolean updateProductStatusIfCurrent(Long id, ProductStatus expectedStatus, ProductStatus targetStatus) {
        int affected = productRepository.updateStatusIfCurrent(id, expectedStatus, targetStatus);
        if (affected == 0) {
            return false;
        }
        invalidateProductStatusCache(id, targetStatus);
        return true;
    }

    private void invalidateProductStatusCache(Long id, ProductStatus status) {
        try {
            redisTemplate.opsForValue().set(KEY_STATUS + id, status.name());
            redisTemplate.delete(KEY_STATIC + id);
        } catch (Exception e) {
            log.warn("Failed to refresh product status cache for product {}", id, e);
        }
    }

    @Transactional
    public void updateInspectionGrade(Long id, String inspectionGrade) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        p.setInspectionGrade(inspectionGrade);
        productRepository.save(p);
        evictProductStaticCache(id);
    }

    private void evictProductStaticCache(Long id) {
        try {
            redisTemplate.delete(KEY_STATIC + id);
        } catch (Exception e) {
            log.warn("Failed to evict product static cache for product {}", id, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> search(String q, Long categoryId, BigDecimal priceMin, BigDecimal priceMax, String sort,
            int page, int size, Long excludeSellerId, Long sellerId, ProductStatus status) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Specification<Product> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            if (status != null && status != ProductStatus.ON_SALE) {
                predicates.add(cb.disjunction());
            } else {
                predicates.add(cb.equal(root.get("status"), ProductStatus.ON_SALE));
            }
            predicates.add(cb.equal(root.get("isDeleted"), Boolean.FALSE));
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim() + "%";
                predicates.add(cb.like(root.get("title"), pattern));
            }
            if (categoryId != null) {
                // Fetch category and its children to filter by all related IDs
                Category cat = categoryRepository.findById(categoryId).orElse(null);
                if (cat != null) {
                    java.util.List<Long> ids = new java.util.ArrayList<>();
                    ids.add(categoryId);
                    // Add children IDs
                    if (cat.getChildren() != null) {
                        cat.getChildren().forEach(c -> ids.add(c.getId()));
                    }
                    predicates.add(root.get("category").get("id").in(ids));
                } else {
                    // Category not found, return empty result
                    predicates.add(cb.disjunction());
                }
            }
            if (priceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceMax));
            }
            // Filter by specific seller
            if (sellerId != null) {
                predicates.add(cb.equal(root.get("seller").get("id"), sellerId));
            } else if (excludeSellerId != null) {
                // 排除当前用户发布的商品
                predicates.add(cb.notEqual(root.get("seller").get("id"), excludeSellerId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Product> products = productRepository.findAll(spec, pageable);
        return (Page<ProductDTO>) products.map((Product p) -> ProductMapper.toDTO(p)).map((ProductDTO dto) -> ProductMapper.enrich(dto));
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "created_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "fav_desc" -> Sort.by(Sort.Direction.DESC, "favoritesCount");
            case "views_desc" -> Sort.by(Sort.Direction.DESC, "viewsCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private void applyCreate(ProductCreateRequest req, Product p) {
        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setPrice(req.price());
        if (req.originalPrice() != null)
            p.setOriginalPrice(req.originalPrice());
        if (req.currency() != null)
            p.setCurrency(req.currency());
        p.setCondition(resolveSellerClaimCondition(req.sellerClaimCondition(), req.condition()));
        if (req.categoryId() != null) {
            Category c = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("类目不存在"));
            p.setCategory(c);
        }
        p.setLocationText(req.locationText());
        p.setShippingPayer(req.shippingPayer());
        p.setTradeTypes(req.tradeTypes());
        if (req.tradeModel() != null) {
            p.setTradeModel(req.tradeModel());
        }
        p.setLat(req.lat());
        p.setLng(req.lng());
        p.getImages().clear();
        if (req.imageKeys() != null) {
            int sort = 0;
            for (String imageKey : req.imageKeys()) {
                ProductImage img = new ProductImage();
                img.setProduct(p);
                img.setImageKey(imageKey);
                img.setSort(sort++);
                img.setIsCover(sort == 1); // 第一张为封面
                p.getImages().add(img);
            }
        }
    }

    private void applyUpdate(ProductUpdateRequest req, Product p, boolean admin) {
        if (req.title() != null)
            p.setTitle(req.title());
        if (req.description() != null)
            p.setDescription(req.description());
        if (req.price() != null)
            p.setPrice(req.price());
        if (req.originalPrice() != null)
            p.setOriginalPrice(req.originalPrice());
        if (req.currency() != null)
            p.setCurrency(req.currency());
        if (admin && req.status() != null)
            p.setStatus(req.status());
        ProductCondition sellerClaimCondition = resolveSellerClaimCondition(req.sellerClaimCondition(), req.condition());
        if (sellerClaimCondition != null)
            p.setCondition(sellerClaimCondition);
        if (req.categoryId() != null) {
            Category c = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("类目不存在"));
            p.setCategory(c);
        }
        if (req.locationText() != null)
            p.setLocationText(req.locationText());
        if (req.shippingPayer() != null)
            p.setShippingPayer(req.shippingPayer());
        if (req.tradeTypes() != null)
            p.setTradeTypes(req.tradeTypes());
        if (req.tradeModel() != null)
            p.setTradeModel(req.tradeModel());
        if (req.lat() != null)
            p.setLat(req.lat());
        if (req.lng() != null)
            p.setLng(req.lng());
        if (req.imageKeys() != null) {
            p.getImages().clear();
            int sort = 0;
            for (String imageKey : req.imageKeys()) {
                ProductImage img = new ProductImage();
                img.setProduct(p);
                img.setImageKey(imageKey);
                img.setSort(sort++);
                img.setIsCover(sort == 1);
                p.getImages().add(img);
            }
        }
    }

    private ProductCondition resolveSellerClaimCondition(ProductCondition sellerClaimCondition, ProductCondition legacyCondition) {
        return sellerClaimCondition != null ? sellerClaimCondition : legacyCondition;
    }

    @Transactional
    public ProductDTO publishProduct(Long id, Long sellerId) {
        return publishProduct(id, sellerId, false);
    }

    @Transactional
    public ProductDTO publishProduct(Long id, Long sellerId, boolean admin) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        assertCanManageProduct(p, sellerId, admin, "无权操作");
        p.setStatus(ProductStatus.ON_SALE);
        Product saved = productRepository.save(p);
        invalidateProductStatusCache(id, ProductStatus.ON_SALE);
        return ProductMapper.enrich(ProductMapper.toDTO(saved));
    }

    @Transactional
    public ProductDTO hideProduct(Long id, Long sellerId) {
        return hideProduct(id, sellerId, false);
    }

    @Transactional
    public ProductDTO hideProduct(Long id, Long sellerId, boolean admin) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        assertCanManageProduct(p, sellerId, admin, "无权操作");
        p.setStatus(ProductStatus.OFF_SHELF);
        Product saved = productRepository.save(p);
        invalidateProductStatusCache(id, ProductStatus.OFF_SHELF);
        return ProductMapper.enrich(ProductMapper.toDTO(saved));
    }

    private void assertCanManageProduct(Product product, Long userId, boolean admin, String message) {
        if (!admin && !Objects.equals(product.getSeller().getId(), userId)) {
            throw new SecurityException(message);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findMyProducts(Long sellerId, String q, java.util.List<ProductStatus> statuses, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Product> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("seller").get("id"), sellerId));
            predicates.add(cb.equal(root.get("isDeleted"), Boolean.FALSE));

            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim() + "%";
                predicates.add(cb.like(root.get("title"), pattern));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Product> products = productRepository.findAll(spec, pageable);
        return (Page<ProductDTO>) products.map((Product p) -> ProductMapper.toDTO(p)).map((ProductDTO dto) -> ProductMapper.enrich(dto));
    }
}
