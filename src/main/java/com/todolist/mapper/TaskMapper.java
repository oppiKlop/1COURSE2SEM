package com.todolist.mapper;

import com.todolist.dto.TaskCreateDto;
import com.todolist.dto.TaskResponseDto;
import com.todolist.dto.TaskUpdateDto;
import com.todolist.model.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toDto(Task task);
}
