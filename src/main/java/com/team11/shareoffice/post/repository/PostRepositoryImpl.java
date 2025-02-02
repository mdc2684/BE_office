package com.team11.shareoffice.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team11.shareoffice.global.security.UserDetailsImpl;
import com.team11.shareoffice.like.entity.Likes;
import com.team11.shareoffice.like.repository.LikeRepository;
import com.team11.shareoffice.post.dto.MainPageResponseDto;
import com.team11.shareoffice.post.entity.Post;
import com.team11.shareoffice.post.entity.QPost;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PostRepositoryImpl extends QuerydslRepositorySupport implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private LikeRepository likeRepository;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Post.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MainPageResponseDto> FilteringAndPaging(UserDetailsImpl userDetails, String keyword, String district, String sorting, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder builder = new BooleanBuilder();


        if (!StringUtils.isEmpty(keyword)) {
            builder.and(post.title.contains(keyword)
                    .or(post.content.contains(keyword))
                    .or(post.location.contains(keyword)));
        }


        if (!StringUtils.isEmpty(district)) {
            builder.and(post.location.contains(district));
        }

        List<OrderSpecifier<?>> sortOrder = getSortOrder(sorting, keyword, district, post);

        QueryResults<Post> results = queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(sortOrder.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();



        List<Post> resultsList = results.getResults();
        List<MainPageResponseDto> dtos = new ArrayList<>();
        for (Post posts : resultsList) {
            Likes likes = null;
            if (userDetails != null) {
                likes = likeRepository.findByMemberIdAndPostId(userDetails.getMember().getId(), posts.getId());
            }
            dtos.add(new MainPageResponseDto(posts, likes));
        }
        return new PageImpl<>(dtos, pageable, results.getTotal());
    }

    private List<OrderSpecifier<?>> getSortOrder(String sorting, String keyword, String district, QPost post) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sorting != null) {
            switch (sorting) {
                case "최근 게시물 순":
                    orders.add(post.createdAt.desc());
                    break;
                case "높은 가격 순":
                    orders.add(post.price.desc());
                    orders.add(post.createdAt.desc());
                    break;
                case "낮은 가격 순":
                    orders.add(post.price.asc());
                    orders.add(post.createdAt.desc());
                    break;
                default:
                    orders.add(post.likeCount.desc());
                    orders.add(post.createdAt.desc());
            }
        } else {
            if (StringUtils.isEmpty(keyword) && StringUtils.isEmpty(district)) {
                orders.add(post.likeCount.desc());
            }
            orders.add(post.createdAt.desc());
        }
        return orders;
    }

}
