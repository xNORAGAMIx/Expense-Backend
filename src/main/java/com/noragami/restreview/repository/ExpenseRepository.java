package com.noragami.restreview.repository;

import com.noragami.restreview.entity.ExpenseEntity;
import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {
    List<ExpenseEntity> findByGroup(GroupEntity group);
    List<ExpenseEntity> findByPaidBy(UserEntity user);
}
