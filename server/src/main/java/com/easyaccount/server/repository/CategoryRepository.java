package com.easyaccount.server.repository;

import com.easyaccount.server.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTypeOrType(Integer type, Integer commonType);
}
