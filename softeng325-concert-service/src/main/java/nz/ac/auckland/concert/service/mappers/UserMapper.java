package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getPassword(),
                user.getLastName(),
                user.getFirstName()
        );
    }

    public static User toDomain(UserDTO userDTO) {
        return new User(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getFirstname(),
                userDTO.getLastname()
        );
    }
}
