package com.photory.dto.request.room;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteUserForceReqDto {

    private Long roomId;
    private Long deletedUserId;
}