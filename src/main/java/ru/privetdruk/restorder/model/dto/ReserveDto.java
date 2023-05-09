package ru.privetdruk.restorder.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.privetdruk.restorder.model.entity.ReserveEntity;
import ru.privetdruk.restorder.model.entity.UserEntity;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveDto {
    private Long id;
    private LocalDate date;
    private UserEntity user;
    private int blockDays;
    private String phoneNumber;
    private ReserveEntity reserve;
}
