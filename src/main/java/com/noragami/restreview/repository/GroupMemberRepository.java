package com.noragami.restreview.repository;

import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.GroupMemberEntity;
import com.noragami.restreview.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    boolean existsByGroupAndUser(GroupEntity group, UserEntity user);

    List<GroupMemberEntity> findByGroup(GroupEntity group);

    List<GroupMemberEntity> findByUser(UserEntity user);

    @Query("SELECT gm FROM GroupMemberEntity gm WHERE gm.user = :user")
    List<GroupMemberEntity> findGroupsByUser(@Param("user") UserEntity user);
}
