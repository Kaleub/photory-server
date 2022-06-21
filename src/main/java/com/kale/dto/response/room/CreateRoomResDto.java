package com.kale.dto.response.room;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateRoomResDto {

    private Long id;
    private String code;
    private String ownerEmail;
    private String title;
    private String password;
    private int participantsCount;
    private Boolean status;
    private long createdTimeInterval;
    private long modifiedTimeInterval;

    @Builder
    public CreateRoomResDto(Long id, String code, String ownerEmail, String title, String password, int participantsCount, Boolean status, long createdTimeInterval, long modifiedTimeInterval) {
        this.id = id;
        this.code = code;
        this.ownerEmail = ownerEmail;
        this.title = title;
        this.password = password;
        this.participantsCount = participantsCount;
        this.status = status;
        this.createdTimeInterval = createdTimeInterval;
        this.modifiedTimeInterval = modifiedTimeInterval;
    }
}
