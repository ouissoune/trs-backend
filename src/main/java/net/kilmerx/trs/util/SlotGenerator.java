package net.kilmerx.trs.util;

import net.kilmerx.trs.dto.SlotRangeRequest;
import net.kilmerx.trs.model.Slot;
import net.kilmerx.trs.model.Teacher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlotGenerator {

    /**
     * Generates one-hour slots from a time range.
     * Only allows ranges with no remaining minutes (must start and end on hour
     * boundaries).
     */
    public static List<Slot> generateSlotsFromRange(SlotRangeRequest rangeRequest, Teacher teacher) {
        LocalDateTime start = rangeRequest.getStartDateTime();
        LocalDateTime end = rangeRequest.getEndDateTime();

        // Validate that there are no remaining minutes
        if ( !(start.getMinute() == end.getMinute()) ) {
            System.out.println(start.getMinute());
            System.out.println(end.getMinute());
            throw new IllegalArgumentException("Start and end times must have the same minutes and seconds");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<Slot> slots = new ArrayList<>();
        LocalDateTime currentStart = start;

        while (currentStart.isBefore(end)) {
            LocalDateTime currentEnd = currentStart.plusHours(1);


            Slot slot = Slot.builder()
                    .teacher(teacher)
                    .startDateTime(currentStart)
                    .endDateTime(currentEnd)
                    .available(true)
                    .build();

            slots.add(slot);
            currentStart = currentEnd;
        }

        return slots;
    }

    /**
     * Generates slots from multiple time ranges.
     */
    public static List<Slot> generateSlotsFromRanges(List<SlotRangeRequest> ranges, Teacher teacher) {
        List<Slot> allSlots = new ArrayList<>();
        for (SlotRangeRequest range : ranges) {
            allSlots.addAll(generateSlotsFromRange(range, teacher));
        }
        return allSlots;
    }
}
