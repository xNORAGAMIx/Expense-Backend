package com.noragami.restreview.service;

import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.UserEntity;
import com.noragami.restreview.io.*;

import java.math.BigDecimal;
import java.util.List;

public interface GroupService {

    GroupEntity createGroup(GroupRequest request, UserEntity user);

    List<GroupEntity> getGroupsByUser(UserEntity user);

    void addMemberToGroup(Long id, String userEmail, UserEntity addedBy);

    List<GroupMemberResponse> getGroupMembers(Long groupId, UserEntity requester);

    void addExpenseToGroup(Long groupId, ExpenseRequest request, UserEntity addedBy);

    List<OweRecordDTO> calculateGroupBalances(Long groupId);

    void settleUp(Long groupId, String fromEmail, String toEmail, BigDecimal amount);

    List<ExpenseResponse> getExpensesInGroup(Long groupId);

    List<ExpenseResponse> getExpensesByUser(UserEntity user);

    List<SettlementResponse> getSettlementsMadeByUser(UserEntity user);

    List<SettlementResponse> getSettlementsReceivedByUser(UserEntity user);

    BigDecimal calculateActualSpentAmount(UserEntity user);

    SpentSummaryDTO getSpentSummary(UserEntity user);

}
