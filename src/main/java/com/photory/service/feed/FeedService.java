package com.photory.service.feed;

import com.photory.domain.feed.Feed;
import com.photory.domain.feed.repository.FeedRepository;
import com.photory.domain.feedimage.FeedImage;
import com.photory.domain.feedimage.repository.FeedImageRepository;
import com.photory.domain.participate.Participate;
import com.photory.domain.participate.repository.ParticipateRepository;
import com.photory.domain.room.Room;
import com.photory.domain.room.repository.RoomRepository;
import com.photory.domain.user.User;
import com.photory.domain.user.repository.UserRepository;
import com.photory.controller.feed.dto.request.DeleteFeedReqDto;
import com.photory.controller.feed.dto.request.ModifyFeedReqDto;
import com.photory.controller.feed.dto.response.ModifyFeedResDto;
import com.photory.controller.feed.dto.response.GetFeedResDto;
import com.photory.common.exception.model.NotFeedOwnerException;
import com.photory.common.exception.model.NotFoundFeedException;
import com.photory.common.exception.model.NotInRoomException;
import com.photory.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ParticipateRepository participateRepository;
    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final S3Service s3Service;

    public void createFeed(String userEmail, List<MultipartFile> images, Long roomId, String title, String content) {
        User user = FeedServiceUtils.findUserByEmail(userRepository, userEmail);
        Room room = FeedServiceUtils.findRoomByRoomId(roomRepository, roomId);

        // 참여하고 있는 방이 아니면 피드 생성할 수 없음
        Optional<Participate> participate = participateRepository.findByRoomAndUser(room, user);
        if (participate.isEmpty()) {
            throw new NotInRoomException();
        }

        Feed feed = Feed.of(room, user, title, content);

        Feed savedFeed = feedRepository.save(feed);

        List<String> fileUrlList = s3Service.uploadFile(images);
        fileUrlList.forEach(file -> {
            FeedImage feedImage = FeedImage.of(savedFeed, file);

            feedImageRepository.save(feedImage);
        });
    }

    public GetFeedResDto getFeed(String userEmail, Long feedId) {
        User user = FeedServiceUtils.findUserByEmail(userRepository, userEmail);

        Optional<Feed> feed = feedRepository.findById(feedId);
        if (feed.isEmpty()) {
            throw new NotFoundFeedException();
        }

        Room room = feed.get().getRoom();

        //방에 참가한 사람만 피드 조회할 수 있음
        Optional<Participate> participating = participateRepository.findByRoomAndUser(room, user);
        if (participating.isEmpty()) {
            throw new NotInRoomException();
        }

        ArrayList<String> imageUrls = new ArrayList<>();
        ArrayList<FeedImage> images = feedImageRepository.findAllByFeed(feed.get());
        images.forEach(image -> {
            imageUrls.add(imageUrls.size(), image.getImageUrl());
        });

        GetFeedResDto getFeedResDto = GetFeedResDto.of(feed.get(), imageUrls);

        return getFeedResDto;
    }

    public ModifyFeedResDto modifyFeed(String userEmail, ModifyFeedReqDto modifyFeedReqDto) {
        User user = FeedServiceUtils.findUserByEmail(userRepository, userEmail);
        Long feedId = modifyFeedReqDto.getFeedId();
        String title = modifyFeedReqDto.getTitle();
        String content = modifyFeedReqDto.getContent();

        Optional<Feed> feed = feedRepository.findById(feedId);
        if (feed.isEmpty()) {
            throw new NotFoundFeedException();
        }

        // 피드 작성자가 아니면 수정할 수 없음
        if (feed.get().getUser().getId() != user.getId()) {
            throw new NotFeedOwnerException();
        }

        feed.get().setTitle(title);
        feed.get().setContent(content);

        Feed modified = feedRepository.save(feed.get());

        ArrayList<String> imageUrls = new ArrayList<>();
        ArrayList<FeedImage> feedImages = feedImageRepository.findAllByFeed(feed.get());
        feedImages.forEach(image -> {
            imageUrls.add(imageUrls.size(), image.getImageUrl());
        });

        ModifyFeedResDto modifyFeedResDto = ModifyFeedResDto.of(modified, imageUrls);

        return modifyFeedResDto;
    }

    public void deleteFeed(String userEmail, DeleteFeedReqDto deleteFeedReqDto) {
        User user = FeedServiceUtils.findUserByEmail(userRepository, userEmail);
        Long feedId = deleteFeedReqDto.getFeedId();

        Optional<Feed> feed = feedRepository.findById(feedId);
        if (feed.isEmpty()) {
            throw new NotFoundFeedException();
        }

        //피드 작성자 아니면 삭제 불가능
        if (feed.get().getUser() != user) {
            throw new NotFeedOwnerException();
        }

        ArrayList<FeedImage> feedImages = feedImageRepository.findAllByFeed(feed.get());
        for (FeedImage image : feedImages) {
            String date[] = image.getImageUrl().split(".com/");
            s3Service.deleteFile(date[1]);
            feedImageRepository.delete(image);
        }

        //전체 피드 삭제
        feedRepository.delete(feed.get());
    }
}