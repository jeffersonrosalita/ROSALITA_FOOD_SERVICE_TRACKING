package com.scc.model;

public record User(
        Integer id,
        String username,
        String password,
        String roles,
        String status
) {
    @Override
    public String toString() {
        return String.format("[%d] Username: %-12s | Role: %-10s | Status: %s",
                id != null ? id : 0, username, roles, status);
    }
}