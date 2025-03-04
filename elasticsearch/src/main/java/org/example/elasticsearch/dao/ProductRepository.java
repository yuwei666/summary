package org.example.elasticsearch.dao;

import org.example.elasticsearch.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {
    // 自定义查询方法
    List<Product> findByName(String name);

    List<Product> findByCategory(String category);
}