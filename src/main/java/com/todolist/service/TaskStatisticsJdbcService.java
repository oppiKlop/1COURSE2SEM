package com.todolist.service;

import com.todolist.dto.PriorityCountDto;
import com.todolist.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskStatisticsJdbcService {
  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<PriorityCountDto> rowMapper = new PriorityCountRowMapper();

  public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Map<Priority, Long> getTasksCountByPriority() {
    List<PriorityCountDto> rows = jdbcTemplate.query(
        "select priority, count(*) as cnt from tasks group by priority",
        rowMapper
    );

    Map<Priority, Long> result = new EnumMap<>(Priority.class);
    for (PriorityCountDto row : rows) {
      result.put(Priority.valueOf(row.getPriority()), row.getCount());
    }
    return result;
  }

  private static class PriorityCountRowMapper implements RowMapper<PriorityCountDto> {
    @Override
    public PriorityCountDto mapRow(ResultSet rs, int rowNum) throws SQLException {
      PriorityCountDto dto = new PriorityCountDto();
      dto.setPriority(rs.getString("priority"));
      dto.setCount(rs.getLong("cnt"));
      return dto;
    }
  }
}

