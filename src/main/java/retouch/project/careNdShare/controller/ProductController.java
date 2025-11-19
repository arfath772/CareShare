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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // ✅ MODIFIED: Accepts 'category' instead of 'condition' to match your HTML form
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam String category, // ✅ Matches form field name
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam MultipartFile image) {

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCategory(category); // Set the category from the form
            product.setType(type);
            product.setDescription(description);

            // ✅ Set a default condition since the old form doesn't have this field
            // We can infer it from category if you want, or just set "Good"
            if ("new".equalsIgnoreCase(category)) {
                product.setCondition("New");
            } else {
                product.setCondition("Good");
            }

            Product savedProduct = productService.addProduct(product, image, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product submitted successfully. Waiting for admin approval.");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log error to console for debugging
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error adding product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

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
            // Use the correct repository method
            List<Product> products = productRepository.findByStatusWithUser(ProductStatus.APPROVED);

            // Apply filters
            if (type != null && !type.equals("all")) {
                products = products.stream()
                        .filter(p -> p.getType().equals(type))
                        .collect(Collectors.toList());
            }

            if (category != null && !category.equals("all")) {
                products = products.stream()
                        .filter(p -> p.getCategory().equalsIgnoreCase(category)) // Filter by category
                        .collect(Collectors.toList());
            }

            if (sort != null) {
                products = sortProducts(products, sort);
            }

            List<ProductResponseDTO> productDTOs = products.stream()
                    .map(ProductResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load products: " + e.getMessage()));
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
                return ResponseEntity.ok(new ProductResponseDTO(product.get()));
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