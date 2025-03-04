package org.example.elasticsearch.controller;

import org.example.elasticsearch.entity.Product;
import org.example.elasticsearch.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class productController {

    @Autowired
    private ProductService productService;

    @Value("${server.port}")
    private String port;

    // 添加产品
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        System.out.println(port);
        return productService.saveProduct(product);
    }

    // 根据名称查询产品
    @GetMapping("/name/{name}")
    public List<Product> getProductsByName(@PathVariable String name) {
        return productService.findByName(name);
    }

    // 根据分类查询产品
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productService.findByCategory(category);
    }

}
