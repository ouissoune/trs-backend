package net.kilmerx.trs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRangeRequest {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
