package org.example.elasticsearch.service;

import org.example.elasticsearch.dao.ProductRepository;
import org.example.elasticsearch.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 保存产品
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // 根据名称查询产品
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    // 根据分类查询产品
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

}
