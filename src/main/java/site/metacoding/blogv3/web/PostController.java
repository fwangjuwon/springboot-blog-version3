package site.metacoding.blogv3.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.config.auth.LoginUser;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.service.PostService;
import site.metacoding.blogv3.web.dto.love.LoveRespDto;
import site.metacoding.blogv3.web.dto.post.PostDetailRespDto;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;


@RequiredArgsConstructor
@Controller
public class PostController {

    private final HttpSession session;
    private final PostService postService;
    // CategoryService 사용하지 말고
    // PostService 사용하세요. 이유는 나중에 category, post글 다 같이 가지고 가야 하기 때문임!!

       // /s/api/post/{id}/love
    @PostMapping("/s/api/post/{postId}/love")
    public ResponseEntity<?> love(@PathVariable Integer postId, @AuthenticationPrincipal LoginUser loginUser) {
        LoveRespDto dto=  postService.좋아요(postId, loginUser.getUser());
        return new ResponseEntity<>(dto, HttpStatus.CREATED); //누가어떤게시물을 좋아요했는지 id, loginuser만 필요 
    }

    @DeleteMapping("/s/api/post/{postId}/love/{loveId}")
    public ResponseEntity<?> unLove(@PathVariable Integer loveId, @AuthenticationPrincipal LoginUser loginUser) {
        //login한 유저의 userId와 
        //love에 있는 userId비교
        postService.좋아요취소(loveId, loginUser.getUser());
        return new ResponseEntity<>(HttpStatus.OK);
    }

@PostMapping("/s/post")
public String write(PostWriteReqDto postWriteReqDto, @AuthenticationPrincipal LoginUser loginUser) {
    //postService.게시글쓰기(postWriteReqDto, loginUser.getUser());
    
    //return "redirect:/user/" + loginUser.getUser().getId() + "/post";
  
    //LoginUser테스트
    System.out.println("---------------------------------------");
    System.out.println(loginUser.getUsername());
    System.out.println("---------------------------------------");

    //PostWriteReqDto 테스트 완료 
    // if (postWriteReqDto.getTitle() == null) {
    //     throw new NullPointerException("타이틀이 없음");
    // }
    // if (postWriteReqDto.getContent() == null) {
    //     throw new NullPointerException("컨텐트 없음");
    // }
    //     if (postWriteReqDto.getCategoryId() == null) {
    //     throw new NullPointerException("카테고리 아이디 없음");
    // }
    return "1";
}


@GetMapping("/s/post/write-form")
public String writeForm(@AuthenticationPrincipal LoginUser loginUser, Model model) {
    List<Category> categorys = postService.게시글쓰기화면(loginUser.getUser());

    if (categorys.size() == 0) {
        throw new CustomException("카테고리 등록이 필요합니다.");
    }

    model.addAttribute("categorys", categorys);
    return "/post/writeForm";
}


    @GetMapping("/user/{pageOwnerId}/post")
    public String postList(Integer categoryId, @PathVariable Integer pageOwnerId, @AuthenticationPrincipal LoginUser loginUser,
            Model model, @PageableDefault(size = 3) Pageable pageable) {

        // SELECT * FROM category WHERE userId = :id
        // 카테고리 가져가세요!!
        PostRespDto postRespDto = null;
        if (categoryId == null) {
            postRespDto = postService.게시글목록보기(pageOwnerId, pageable);
        } else {
            postRespDto = postService.게시글카테고리별보기(pageOwnerId, categoryId, pageable);
        }

        model.addAttribute("postRespDto", postRespDto);
        return "/post/list";
    }
    
        @GetMapping("/test/user/{id}/post")
    public @ResponseBody PostRespDto testPostList(Integer categoryId, @PathVariable Integer id,
            @AuthenticationPrincipal LoginUser loginUser,
            Model model,
            @PageableDefault(size = 3) Pageable pageable) {
        // SELECT * FROM category WHERE userId = :id
        // 카테고리 가져가세요!!
        PostRespDto postRespDto = null;

        if (categoryId == null) {
            postRespDto = postService.게시글목록보기(id, pageable);
        } else {
            postRespDto = postService.게시글카테고리별보기(id, categoryId, pageable);
        }

        return postRespDto;
    }

        @GetMapping("/post/{id}")
        public String detail(@PathVariable Integer id, Model model, @AuthenticationPrincipal LoginUser loginUser) {
 PostDetailRespDto postDetailRespDto = null;
            if (loginUser == null) {
            postDetailRespDto = postService.게시글상세보기(id);
        } else {
            postDetailRespDto = postService.게시글상세보기(id, loginUser.getUser());
        }

        model.addAttribute("data", postDetailRespDto);
            return "/post/detail";
        }
        
    @DeleteMapping("/s/api/post/{id}")
    public ResponseEntity<?> postDelete(@PathVariable Integer id, @AuthenticationPrincipal LoginUser loginUser) {

        User principal = loginUser.getUser();

        postService.게시글삭제(id, principal);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    }
