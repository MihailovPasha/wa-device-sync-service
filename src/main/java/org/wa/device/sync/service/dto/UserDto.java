package org.wa.device.sync.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wa.device.sync.service.dto.enumeration.RoleEnum;
import org.wa.device.sync.service.dto.enumeration.StatusEnum;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String phone;
    private StatusEnum status;
    private Set<RoleEnum> roles;
    private String googleRefreshToken;
}
