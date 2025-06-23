package com.noragami.restreview.repository;

import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByCreatedBy(UserEntity user);
    GroupEntity findByGroupId(String groupId);
}
