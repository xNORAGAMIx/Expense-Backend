package com.noragami.restreview.service;

import com.noragami.restreview.entity.*;
import com.noragami.restreview.io.*;
import com.noragami.restreview.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository  expenseParticipantRepository;
    private final SettlementRepository settlementRepository;

    @Override
    public GroupEntity createGroup(GroupRequest request, UserEntity user) {
        GroupEntity group = GroupEntity.builder()
                .groupId(UUID.randomUUID().toString())
                .name(request.getName())
                .createdBy(user)
                .build();

        return groupRepository.save(group);
    }

    @Override
    public List<GroupEntity> getGroupsByUser(UserEntity user) {
        List<GroupEntity> createdGroups = groupRepository.findByCreatedBy(user);
        List<GroupMemberEntity> memberships = groupMemberRepository.findByUser(user);

        List<GroupEntity> memberGroups = memberships.stream()
                .map(GroupMemberEntity::getGroup)
                .toList();

        Set<GroupEntity> allGroups = new HashSet<>();
        allGroups.addAll(createdGroups);
        allGroups.addAll(memberGroups);

        return new ArrayList<>(allGroups);
    }

    @Override
    @Transactional
    public void addMemberToGroup(Long id, String userEmail, UserEntity addedBy) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Optional: Only group creator can add members
        if (!group.getCreatedBy().getId().equals(addedBy.getId())) {
            throw new RuntimeException("Only the group creator can add members.");
        }

        // Check if the user (to be added) exists
        UserEntity userToAdd = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    System.out.println("No user exists with this email.");
                    return new RuntimeException("No user exists with this email.");
                });

        // Check if already added
        boolean alreadyMember = groupMemberRepository.existsByGroupAndUser(group, userToAdd);
        if (alreadyMember) {
            System.out.println("User is already a member of this group.");
            throw new RuntimeException("User is already a member of this group.");
        }

        // Add to group
        GroupMemberEntity groupMember = GroupMemberEntity.builder()
                .group(group)
                .user(userToAdd)
                .build();

        groupMemberRepository.save(groupMember);
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(Long groupId, UserEntity requester) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Optional: Check if requester is part of group
        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, requester)
                || group.getCreatedBy().getId().equals(requester.getId());

        if (!isMember) {
            throw new RuntimeException("Access denied. You're not part of this group.");
        }

        List<GroupMemberEntity> members = groupMemberRepository.findByGroup(group);

        List<GroupMemberResponse> responses = members.stream()
                .map(member -> new GroupMemberResponse(
                        member.getUser().getName(),
                        member.getUser().getEmail()))
                .collect(Collectors.toList());

        // Include creator - requester may not be the creator
        responses.add(new GroupMemberResponse(group.getCreatedBy().getName(), group.getCreatedBy().getEmail()));

        return responses;
    }

    @Override
    @Transactional
    public void addExpenseToGroup(Long groupId, ExpenseRequest request, UserEntity addedBy) {
        // Check if group exists
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Check if payer exists
        UserEntity payer = userRepository.findByEmail(request.getPaidByEmail())
                .orElseThrow(() -> new RuntimeException("Payer with email " + request.getPaidByEmail() + " not found"));

        // Check if payer is group member or creator
        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, payer)
                || group.getCreatedBy().getId().equals(payer.getId());

        if (!isMember) {
            throw new RuntimeException("Payer is not a member of the group.");
        }

        // Create and save the expense
        ExpenseEntity expense = ExpenseEntity.builder()
                .group(group)
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .paidBy(payer)
                .build();
        expenseRepository.save(expense);

        // Get all group members
        List<UserEntity> members = groupMemberRepository.findByGroup(group)
                .stream().map(GroupMemberEntity::getUser).collect(Collectors.toList());

        // Include group creator if not already included
        if (members.stream().noneMatch(user -> user.getId().equals(group.getCreatedBy().getId()))) {
            members.add(group.getCreatedBy());
        }

        // Calculate and assign even split
        BigDecimal perHead = request.getAmount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        for (UserEntity member : members) {
            ExpenseParticipantEntity ep = ExpenseParticipantEntity.builder()
                    .expense(expense)
                    .user(member)
                    .share(perHead)
                    .build();
            expenseParticipantRepository.save(ep);
        }
    }

    @Override
    public List<OweRecordDTO> calculateGroupBalances(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<ExpenseEntity> expenses = expenseRepository.findByGroup(group);

        Map<UserEntity, BigDecimal> paidMap = new HashMap<>();
        Map<UserEntity, BigDecimal> owedMap = new HashMap<>();

        // STEP 1: Process expenses
        for (ExpenseEntity expense : expenses) {
            UserEntity payer = expense.getPaidBy();
            paidMap.put(payer, paidMap.getOrDefault(payer, BigDecimal.ZERO).add(expense.getAmount()));

            List<ExpenseParticipantEntity> participants = expenseParticipantRepository.findByExpense(expense);
            for (ExpenseParticipantEntity ep : participants) {
                UserEntity user = ep.getUser();
                owedMap.put(user, owedMap.getOrDefault(user, BigDecimal.ZERO).add(ep.getShare()));
            }
        }

        // STEP 2: Calculate net balances
        Map<UserEntity, BigDecimal> netBalances = new HashMap<>();
        Set<UserEntity> allUsers = new HashSet<>();
        allUsers.addAll(paidMap.keySet());
        allUsers.addAll(owedMap.keySet());

        for (UserEntity user : allUsers) {
            BigDecimal paid = paidMap.getOrDefault(user, BigDecimal.ZERO);
            BigDecimal owed = owedMap.getOrDefault(user, BigDecimal.ZERO);
            netBalances.put(user, paid.subtract(owed));
        }

        // STEP 3: Adjust for manual settlements
        List<SettlementEntity> settlements = settlementRepository.findByGroup(group);
        for (SettlementEntity s : settlements) {
            UserEntity from = s.getFromUser();
            UserEntity to = s.getToUser();
            BigDecimal amount = s.getAmount();

            // 'from' gave money, reduce their debt
            netBalances.put(from, netBalances.getOrDefault(from, BigDecimal.ZERO).add(amount));
            // 'to' received money, reduce their credit
            netBalances.put(to, netBalances.getOrDefault(to, BigDecimal.ZERO).subtract(amount));
        }

        // STEP 4: Sort creditors and debtors
        List<Map.Entry<UserEntity, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<UserEntity, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<UserEntity, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }

        // Sort for deterministic resolution
        creditors.sort(Comparator.comparing(e -> e.getKey().getEmail()));
        debtors.sort(Comparator.comparing(e -> e.getKey().getEmail()));

        // STEP 5: Match debtors to creditors
        List<OweRecordDTO> results = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            UserEntity debtor = debtors.get(i).getKey();
            UserEntity creditor = creditors.get(j).getKey();

            BigDecimal debtAmount = debtors.get(i).getValue().abs();
            BigDecimal creditAmount = creditors.get(j).getValue();

            BigDecimal transfer = debtAmount.min(creditAmount).setScale(2, RoundingMode.HALF_UP);

            results.add(new OweRecordDTO(
                    debtor.getName(),
                    creditor.getName(),
                    transfer
            ));

            // Update balances
            BigDecimal updatedDebtor = netBalances.get(debtor).add(transfer);
            BigDecimal updatedCreditor = netBalances.get(creditor).subtract(transfer);

            netBalances.put(debtor, updatedDebtor);
            netBalances.put(creditor, updatedCreditor);

            // Reflect back into lists
            debtors.set(i, Map.entry(debtor, updatedDebtor));
            creditors.set(j, Map.entry(creditor, updatedCreditor));

            if (updatedDebtor.compareTo(BigDecimal.ZERO) == 0) i++;
            if (updatedCreditor.compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return results;
    }

    @Override
    public void settleUp(Long groupId, String fromEmail, String toEmail, BigDecimal amount) {
        // 1. Validate group exists
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

// 2. Validate users
        UserEntity fromUser = userRepository.findByEmail(fromEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        UserEntity toUser = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new RuntimeException("To user not found"));

        if (fromUser.getEmail().equalsIgnoreCase(toUser.getEmail())) {
            throw new RuntimeException("You cannot settle with yourself.");
        }

// 3. Ensure both are members of the group (including creator)
        boolean isFromUserInGroup = group.getCreatedBy().equals(fromUser) ||
                groupMemberRepository.existsByGroupAndUser(group, fromUser);
        boolean isToUserInGroup = group.getCreatedBy().equals(toUser) ||
                groupMemberRepository.existsByGroupAndUser(group, toUser);

        if (!isFromUserInGroup) {
            throw new RuntimeException("You are not a member of this group.");
        }
        if (!isToUserInGroup) {
            throw new RuntimeException("The user you're settling with is not in the group.");
        }

// 4. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

// 5. Save settlement
        SettlementEntity settlement = SettlementEntity.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .settledAt(new Timestamp(System.currentTimeMillis()))
                .build();

        settlementRepository.save(settlement);

    }

    @Override
    public List<ExpenseResponse> getExpensesInGroup(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<ExpenseEntity> expenses = expenseRepository.findByGroup(group);

        return expenses.stream().map(expense -> ExpenseResponse.builder()
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidByEmail(expense.getPaidBy().getEmail())
                .paidByName(expense.getPaidBy().getName())
                .createdAt(expense.getCreatedAt())
                .build()).toList();
    }

    public List<ExpenseResponse> getExpensesByUser(UserEntity user) {
        List<ExpenseEntity> expenses = expenseRepository.findByPaidBy(user);

        return expenses.stream().map(expense -> ExpenseResponse.builder()
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidByEmail(expense.getPaidBy().getEmail())
                .paidByName(expense.getPaidBy().getName())
                .createdAt(expense.getCreatedAt())
                .build()).toList();
    }

    @Override
    public List<SettlementResponse> getSettlementsMadeByUser(UserEntity user) {
        List<SettlementEntity> settlements = settlementRepository.findByFromUser(user);

        return settlements.stream().map(settlement -> SettlementResponse.builder()
                .groupName(settlement.getGroup().getName())
                .toEmail(settlement.getToUser().getEmail())
                .toName(settlement.getToUser().getName())
                .amount(settlement.getAmount())
                .settledAt(settlement.getSettledAt())
                .build()).toList();
    }

    @Override
    public List<SettlementResponse> getSettlementsReceivedByUser(UserEntity user) {
        List<SettlementEntity> settlements = settlementRepository.findByToUser(user);

        return settlements.stream().map(settlement -> SettlementResponse.builder()
                .groupName(settlement.getGroup().getName())
                .fromEmail(settlement.getFromUser().getEmail())  // email of the person who sent
                .fromName(settlement.getFromUser().getName())
                .amount(settlement.getAmount())
                .settledAt(settlement.getSettledAt())
                .build()).toList();
    }

    @Override
    @Transactional
    public BigDecimal calculateActualSpentAmount(UserEntity user) {
        // 1. Get all groups where the user is a member
        // System.out.println(" User "+user); -> working
        List<GroupMemberEntity> groupMember = groupMemberRepository.findByUser(user);
        List<GroupEntity> createdGroup = groupRepository.findByCreatedBy(user);
        // System.out.println(" Member "+groupMember.size()); -> since creator is not group member
        List<GroupEntity> groups = new ArrayList<>();

        groups = groupMember
                .stream()
                .map(GroupMemberEntity::getGroup)
                .toList();

        groups = groups.isEmpty() ? createdGroup : groups;

        //System.out.println(groupMemberRepository.findGroupsByUser(user));

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;

        //System.out.println(" group size "+ groups.size());
        for (GroupEntity group : groups) {
            List<ExpenseEntity> expenses = expenseRepository.findByGroup(group);

            for (ExpenseEntity expense : expenses) {
                // 2. Add total amount the user paid
                // System.out.println(" in expense "+ expense.getPaidBy());
                if (expense.getPaidBy().equals(user)) {
                    totalPaid = totalPaid.add(expense.getAmount());
                }

                // 3. Calculate user’s owed share
                List<ExpenseParticipantEntity> participants = expenseParticipantRepository.findByExpense(expense);
                boolean userFoundInParticipants = false;

                for (ExpenseParticipantEntity ep : participants) {
                    if (ep.getUser().equals(user)) {
                        totalOwed = totalOwed.add(ep.getShare());
                        userFoundInParticipants = true;
                        break;
                    }
                }

//                // 4. If user not in participants but is the payer, assume equal split
//                if (!userFoundInParticipants && expense.getPaidBy().equals(user)) {
//                    int numParticipants = participants.size();
//                    if (numParticipants > 0) {
//                        BigDecimal share = expense.getAmount()
//                                .divide(BigDecimal.valueOf(numParticipants), 2, RoundingMode.HALF_UP);
//                        totalOwed = totalOwed.add(share);
//                    }
//                }
            }
        }

        System.out.println(totalPaid+ " " + totalOwed);
        // 5. Actual cost borne by user = paid - owed
        return totalPaid.subtract(totalOwed).setScale(2, RoundingMode.HALF_UP);
    }


    public SpentSummaryDTO getSpentSummary(UserEntity user) {
        List<GroupMemberEntity> groupMember = groupMemberRepository.findByUser(user);
        List<GroupEntity> createdGroup = groupRepository.findByCreatedBy(user);
        List<GroupEntity> groups = new ArrayList<>();

        groups = groupMember
                .stream()
                .map(GroupMemberEntity::getGroup)
                .toList();

        groups = groups.isEmpty() ? createdGroup : groups;

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;

        Map<String, GroupWiseSpent> groupMap = new HashMap<>();
        Map<String, CategoryWiseSpent> categoryMap = new HashMap<>();

        for (GroupEntity group : groups) {
            List<ExpenseEntity> expenses = expenseRepository.findByGroup(group);

            for (ExpenseEntity expense : expenses) {
                String groupName = group.getName();
                String category = expense.getCategory(); // Ensure this field exists

                // Init group and category records
                groupMap.putIfAbsent(groupName, new GroupWiseSpent(groupName, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
                categoryMap.putIfAbsent(category, new CategoryWiseSpent(category, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

                boolean isUserPayer = expense.getPaidBy().equals(user);
                List<ExpenseParticipantEntity> participants = expenseParticipantRepository.findByExpense(expense);

                for (ExpenseParticipantEntity ep : participants) {
                    if (ep.getUser().equals(user)) {
                        BigDecimal share = ep.getShare();
                        totalOwed = totalOwed.add(share);
                        groupMap.get(groupName).setTotalOwed(groupMap.get(groupName).getTotalOwed().add(share));
                        categoryMap.get(category).setTotalOwed(categoryMap.get(category).getTotalOwed().add(share));
                    }
                }

                if (isUserPayer) {
                    BigDecimal amt = expense.getAmount();
                    totalPaid = totalPaid.add(amt);
                    groupMap.get(groupName).setTotalPaid(groupMap.get(groupName).getTotalPaid().add(amt));
                    categoryMap.get(category).setTotalPaid(categoryMap.get(category).getTotalPaid().add(amt));
                }
            }
        }

        // Compute actual spent
        for (GroupWiseSpent g : groupMap.values()) {
            g.setActualSpent(g.getTotalPaid().subtract(g.getTotalOwed()).setScale(2, RoundingMode.HALF_UP));
        }
        for (CategoryWiseSpent c : categoryMap.values()) {
            c.setActualSpent(c.getTotalPaid().subtract(c.getTotalOwed()).setScale(2, RoundingMode.HALF_UP));
        }

        return new SpentSummaryDTO(
                totalPaid.subtract(totalOwed).setScale(2, RoundingMode.HALF_UP),
                new ArrayList<>(groupMap.values()),
                new ArrayList<>(categoryMap.values())
        );
    }


}
