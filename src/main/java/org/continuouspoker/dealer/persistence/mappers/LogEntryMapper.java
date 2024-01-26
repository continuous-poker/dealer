package org.continuouspoker.dealer.persistence.mappers;

import org.continuouspoker.dealer.LogEntry;
import org.continuouspoker.dealer.persistence.entities.LogEntryBE;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogEntryMapper {
    LogEntryBE toEntity(LogEntry logEntry);
    LogEntry toDto(LogEntryBE logEntryBE);
}
