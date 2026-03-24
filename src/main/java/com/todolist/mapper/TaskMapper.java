package com.todolist.mapper;

import com.todolist.dto.TaskCreateDto;
import com.todolist.dto.TaskResponseDto;
import com.todolist.dto.TaskUpdateDto;
import com.todolist.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  Task toEntity(TaskCreateDto dto);

  void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);
}
