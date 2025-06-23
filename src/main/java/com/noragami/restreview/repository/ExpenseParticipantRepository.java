package com.noragami.restreview.repository;

import com.noragami.restreview.entity.ExpenseEntity;
import com.noragami.restreview.entity.ExpenseParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipantEntity, Long> {
    List<ExpenseParticipantEntity> findByExpense(ExpenseEntity expense);
}
