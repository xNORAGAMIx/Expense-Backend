package com.noragami.restreview.repository;

import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.SettlementEntity;
import com.noragami.restreview.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByGroup(GroupEntity group);

    List<SettlementEntity> findByFromUser(UserEntity user);

    List<SettlementEntity> findByToUser(UserEntity user);

    List<SettlementEntity> findByGroupAndFromUser(GroupEntity group, UserEntity fromUser);

    List<SettlementEntity> findByGroupAndToUser(GroupEntity group, UserEntity toUser);
}
