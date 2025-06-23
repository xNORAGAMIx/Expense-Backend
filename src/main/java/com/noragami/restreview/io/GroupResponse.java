package com.noragami.restreview.io;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String groupId;
    private String name;
    private String createdByEmail;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private List<GroupMemberResponse> members;
    private List<ExpenseResponse> expenses;
}
