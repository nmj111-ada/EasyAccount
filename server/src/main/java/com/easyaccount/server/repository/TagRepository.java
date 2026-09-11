package com.easyaccount.server.repository;

import com.easyaccount.server.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByCategoryId(Long categoryId);
}
