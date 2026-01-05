package retouch.project.careNdShare.controller;

import org.springframework.http.HttpStatus;
import retouch.project.careNdShare.dto.ProductResponseDTO;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.repository.ProductRepository;
import retouch.project.careNdShare.service.AuthService;
import retouch.project.careNdShare.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam(required = false) String description,
            @RequestParam String condition, // REQUIRED condition parameter
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            // Validate images
            List<MultipartFile> allImages = new ArrayList<>();
            if (images != null && images.length > 0) {
                for (MultipartFile img : images) {
                    if (img != null && !img.isEmpty() && !img.getOriginalFilename().isEmpty()) {
                        allImages.add(img);
                    }
                }
            }

            if (allImages.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please upload at least one product image"));
            }

            // Validate condition
            if (condition == null || condition.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product condition is required"));
            }

            // DEBUG LOG
            System.out.println("=== ProductController.addProduct DEBUG ===");
            System.out.println("Received condition: '" + condition + "'");
            System.out.println("Name: " + name);
            System.out.println("Price: " + price);
            System.out.println("Category: " + category);
            System.out.println("Type: " + type);
            System.out.println("Description: " + description);

            // Create and set product
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCategory(category);
            product.setType(type);
            product.setDescription(description != null ? description : "");
            product.setCondition(condition); // THIS IS THE KEY LINE

            System.out.println("After setting - product.getCondition(): " + product.getCondition());
            System.out.println("After setting - product.getProductCondition(): " + product.getProductCondition());

            // Process images
            MultipartFile[] imagesArray = allImages.toArray(new MultipartFile[0]);
            Product savedProduct = productService.addProduct(product, imagesArray, currentUser);

            System.out.println("After service - savedProduct.getCondition(): " + savedProduct.getCondition());
            System.out.println("After service - savedProduct.getProductCondition(): " + savedProduct.getProductCondition());
            System.out.println("=== DEBUG END ===");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product submitted successfully with " + allImages.size() + " images. Waiting for admin approval.");
            response.put("product", savedProduct);
            response.put("totalImages", allImages.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error adding product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/add-single")
    public ResponseEntity<?> addProductSingleImage(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam(required = false) String description,
            @RequestParam String condition,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please upload a product image"));
            }

            if (condition == null || condition.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product condition is required"));
            }

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCategory(category);
            product.setType(type);
            product.setDescription(description != null ? description : "");
            product.setCondition(condition);

            Product savedProduct = productService.addProduct(product, image, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product submitted successfully. Waiting for admin approval.");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error adding product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ALL OTHER METHODS REMAIN EXACTLY THE SAME
    @GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }
            List<ProductResponseDTO> products = productService.getUserProductsDTO(currentUser.getId());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/my-products/{status}")
    public ResponseEntity<?> getMyProductsByStatus(@PathVariable String status) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }
            ProductStatus productStatus;
            try {
                productStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }
            List<Product> products = productService.getUserProductsByStatus(currentUser.getId(), productStatus);
            List<ProductResponseDTO> productDTOs = products.stream()
                    .map(ProductResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableProducts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {
        try {
            List<Product> products = productRepository.findByStatus(ProductStatus.APPROVED);
            if (type != null && !type.equals("all")) {
                products = products.stream()
                        .filter(p -> p.getType().equals(type))
                        .collect(Collectors.toList());
            }
            if (category != null && !category.equals("all")) {
                products = products.stream()
                        .filter(p -> p.getCategory().equals(category))
                        .collect(Collectors.toList());
            }
            if (sort != null) {
                products = sortProducts(products, sort);
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load products"));
        }
    }

    @GetMapping("/available/{type}")
    public ResponseEntity<List<Product>> getAvailableProductsByType(@PathVariable String type) {
        try {
            List<Product> availableProducts = productRepository.findByStatusAndType(ProductStatus.APPROVED, type);
            return ResponseEntity.ok(availableProducts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productRepository.findByIdWithUser(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found or not approved"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load product details"));
        }
    }

    private List<Product> sortProducts(List<Product> products, String sortType) {
        switch(sortType) {
            case "newest":
                return products.stream()
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());
            case "oldest":
                return products.stream()
                        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .collect(Collectors.toList());
            case "price_low":
                return products.stream()
                        .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                        .collect(Collectors.toList());
            case "price_high":
                return products.stream()
                        .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                        .collect(Collectors.toList());
            case "name_asc":
                return products.stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .collect(Collectors.toList());
            case "name_desc":
                return products.stream()
                        .sorted((a, b) -> b.getName().compareToIgnoreCase(a.getName()))
                        .collect(Collectors.toList());
            default:
                return products;
        }
    }
}