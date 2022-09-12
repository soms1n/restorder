package ru.privetdruk.restorder.mapper;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import ru.privetdruk.restorder.model.dto.ScheduleDto;
import ru.privetdruk.restorder.model.entity.ScheduleEntity;

@Service
public class ScheduleMapper {
    public ScheduleEntity toEntity(ScheduleDto dto) {
        ScheduleEntity entity = new ScheduleEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
