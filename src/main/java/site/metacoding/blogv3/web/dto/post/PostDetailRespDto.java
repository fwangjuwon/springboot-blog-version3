package site.metacoding.blogv3.web.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.metacoding.blogv3.domain.post.Post;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostDetailRespDto {
    private Post post;
    private boolean isPageOwner; //getter는 .isPageOwner(){} setter는 .setPageOwner(){} 로 만들어진다. (롬복일 때) -> 머스타치에서 쓸 때 중요하다 
    private boolean isLove; //좋아요하면 true, 아니면false
}
