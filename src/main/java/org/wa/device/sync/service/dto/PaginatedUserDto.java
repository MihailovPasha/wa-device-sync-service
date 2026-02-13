package org.wa.device.sync.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedUserDto {
    private List<UserDto> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
}
