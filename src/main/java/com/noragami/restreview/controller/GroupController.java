package com.noragami.restreview.controller;

import com.noragami.restreview.entity.GroupEntity;
import com.noragami.restreview.entity.UserEntity;
import com.noragami.restreview.io.*;
import com.noragami.restreview.repository.UserRepository;
import com.noragami.restreview.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    @PostMapping("/create-group")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GroupEntity> createGroup (@RequestBody GroupRequest request, @CurrentSecurityContext(expression = "authentication?.name") String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
        GroupEntity group = groupService.createGroup(request, user);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getGroups(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<GroupResponse> groupDTOs = groupService.getGroupsByUser(user).stream()
                .map(group -> GroupResponse.builder()
                        .id(group.getId())
                        .groupId(group.getGroupId())
                        .name(group.getName())
                        .createdByEmail(group.getCreatedBy().getEmail())
                        .createdAt(group.getCreatedAt())
                        .updatedAt(group.getUpdatedAt())
                        .members(group.getMembers().stream()
                                .map(member -> {
                                    UserEntity u = member.getUser();
                                    return GroupMemberResponse.builder()
                                            .name(u.getName())
                                            .email(u.getEmail())
                                            .build();
                                }).toList())
                        .expenses(group.getExpenses().stream()
                                .map(expense -> ExpenseResponse.builder()
                                        .description(expense.getDescription())
                                        .amount(expense.getAmount())
                                        .paidByEmail(expense.getPaidBy().getEmail())
                                        .paidByName(expense.getPaidBy().getName())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return ResponseEntity.ok(groupDTOs);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<String> addMemberToGroup(
            @PathVariable Long id,
            @RequestBody GroupMemberRequest request,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity addedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("User not found.");
                    return new RuntimeException("User not found");
                });

        groupService.addMemberToGroup(id, request.getEmail(), addedBy);
        return ResponseEntity.ok("Member added successfully.");
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable Long groupId,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId, user);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<String> addExpense(
            @PathVariable Long groupId,
            @RequestBody ExpenseRequest request,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        groupService.addExpenseToGroup(groupId, request, user);
        return ResponseEntity.ok("Expense added successfully.");
    }


    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<OweRecordDTO>> getGroupBalances(@PathVariable Long groupId) {
        List<OweRecordDTO> balances = groupService.calculateGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/{groupId}/settle")
    public ResponseEntity<String> settleUp(
            @PathVariable Long groupId,
            @RequestBody SettlementRequest request,
            @CurrentSecurityContext(expression = "authentication?.name") String fromEmail
    ) {
        groupService.settleUp(groupId, fromEmail, request.getToEmail(), request.getAmount());
        return ResponseEntity.ok("Settlement recorded successfully.");
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpensesInGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getExpensesInGroup(groupId));
    }

    @GetMapping("/my-expenses")
    public ResponseEntity<List<ExpenseResponse>> getMyExpenses(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(groupService.getExpensesByUser(user));
    }


    @GetMapping("/my-settlements")
    public ResponseEntity<List<SettlementResponse>> getMySettlements(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(groupService.getSettlementsMadeByUser(user));
    }


    @GetMapping("/received-settlements")
    public ResponseEntity<List<SettlementResponse>> getReceivedSettlements(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(groupService.getSettlementsReceivedByUser(user));
    }

    @GetMapping("/total-spent")
    public ResponseEntity<BigDecimal> getActualAmountSpent(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal actualSpent = groupService.calculateActualSpentAmount(user);
        return ResponseEntity.ok(actualSpent);
    }

    @GetMapping("/spent-summary")
    public ResponseEntity<SpentSummaryDTO> getSpentSummary(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(groupService.getSpentSummary(user));
    }

}
