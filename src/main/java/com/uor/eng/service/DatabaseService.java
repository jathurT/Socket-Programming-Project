// src/main/java/com/networkmonitor/service/DatabaseService.java
package com.uor.eng.service;

import com.uor.eng.model.Host;
import com.uor.eng.model.HostType;
import com.uor.eng.model.Log;
import com.uor.eng.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DatabaseService {
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/network_monitoring";
  private static final String DB_USER = "postgres";
  private static final String DB_PASSWORD = "Abc@123456";

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
  }

  public Optional<User> loginUser(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.setString(2, password);

      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        return Optional.of(user);
      }
    } catch (SQLException e) {
      log.error("Error logging in user", e);
    }
    return Optional.empty();
  }

  public void registerUser(User user) throws SQLException {
    String sql = "INSERT INTO users (full_name, email, username, password) VALUES (?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, user.getFullName());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getUsername());
      pstmt.setString(4, user.getPassword());

      pstmt.executeUpdate();

      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) {
        user.setId(rs.getLong(1));
      }
    }
  }

  public List<Host> getUserHosts(Long userId) {
    List<Host> hosts = new ArrayList<>();
    String sql = "SELECT * FROM hosts WHERE user_id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, userId);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        Host host = new Host();
        host.setId(rs.getLong("id"));
        host.setName(rs.getString("name"));
        host.setHostname(rs.getString("hostname"));
        host.setType(HostType.valueOf(rs.getString("type")));
        host.setUserId(userId);
        host.setStatus(rs.getString("status"));
        hosts.add(host);
      }
    } catch (SQLException e) {
      log.error("Error fetching user hosts", e);
    }
    return hosts;
  }

  public void addHost(Host host) throws SQLException {
    String sql = "INSERT INTO hosts (name, hostname, type, user_id, status) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, host.getName());
      pstmt.setString(2, host.getHostname());
      pstmt.setString(3, host.getType().name());
      pstmt.setLong(4, host.getUserId());
      pstmt.setString(5, "UNKNOWN");

      pstmt.executeUpdate();

      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) {
        host.setId(rs.getLong(1));
      }
    }
  }

  public void addLog(Log log) throws SQLException {
    String sql = "INSERT INTO logs (host_id, time, status, response_time, old_status) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, log.getHostId());
      pstmt.setTimestamp(2, Timestamp.valueOf(log.getTime()));
      pstmt.setString(3, log.getStatus());
      pstmt.setDouble(4, log.getResponseTime() != null ? log.getResponseTime() : 0);
      pstmt.setString(5, log.getOldStatus());

      pstmt.executeUpdate();
    }
  }

  public List<Log> getHostLogs(Long hostId, int limit) {
    List<Log> logs = new ArrayList<>();
    String sql = "SELECT * FROM logs WHERE host_id = ? ORDER BY time DESC LIMIT ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, hostId);
      pstmt.setInt(2, limit);

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        Log log = new Log();
        log.setId(rs.getLong("id"));
        log.setHostId(hostId);
        log.setTime(rs.getTimestamp("time").toLocalDateTime());
        log.setStatus(rs.getString("status"));
        log.setResponseTime(rs.getDouble("response_time"));
        log.setOldStatus(rs.getString("old_status"));
        logs.add(log);
      }
    } catch (SQLException e) {
      log.error("Error fetching host logs", e);
    }
    return logs;
  }
}

