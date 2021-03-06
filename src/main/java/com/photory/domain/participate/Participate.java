package com.photory.domain.participate;

import com.photory.domain.common.AuditingTimeEntity;
import com.photory.domain.room.Room;
import com.photory.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participate extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ROOM_ID")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public Participate(Room room, User user) {
        this.room = room;
        this.user = user;
    }

    public static Participate of(Room room, User user) {
        return Participate.builder()
                .room(room)
                .user(user)
                .build();
    }
}
