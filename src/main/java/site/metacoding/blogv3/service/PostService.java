package site.metacoding.blogv3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.domain.user.UserRepository;
import site.metacoding.blogv3.domain.visit.Visit;
import site.metacoding.blogv3.domain.visit.VisitRepository;
import site.metacoding.blogv3.handler.ex.CustomApiException;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostDetailRespDto;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {


    @Value("${file.path}")
    private String uploadFolder;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    public List<Category> 게시글쓰기화면(User principal){
        return categoryRepository.findByUserId(principal.getId());
    }

    //하나의 서비스는 여러가지 일을 한번에 처리한다(여러가지 일이 하나의 트랜잭션이다)
    //단점: 여러가지 로직이 하나의 서비스에 공존하고 있다. -> 디버깅 하기 힘듦
    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto, User principal){

        //1. 파일 저장 (UUID로 변경해서 저장)

        //(1) UUID로 파일 쓰고 경로 리턴받기 
        String thumnail = null;
        if (!postWriteReqDto.getThumnailFile().isEmpty()) {
            thumnail = UtilFileUpload.write(uploadFolder, postWriteReqDto.getThumnailFile());
        }     // Category category = new Category();
        // category.set setId(postWriteReqDto.getCategoryId());

        //(2) 카테고리 있는지 확인
    Optional<Category> categoryOp = categoryRepository.findById(postWriteReqDto.getCategoryId());
        
    //(3) post DB저장
    if(categoryOp.isPresent()){
        Post post = postWriteReqDto.toEntity(thumnail, principal, categoryOp.get());
        postRepository.save(post);
    }else{
throw new CustomException("해당카테고리가 존자해지 않습니다");
    }


        // postRepository.mSave(
        //     postWriteReqDto.getCategoryId(), 
        //     principal.getId(),
        //     postWriteReqDto.getTitle(),
        //     postWriteReqDto.getContent(),
        //     thumnail);

        // 2. 파일명을 Post 오브젝트에 thumnail로 옮겨야 함

        // 3. title,content도 Post 오브젝트에 옮기고

        // 4. userId도 Post 오브젝트에 옮기기

        // 5. categoryId도 Post 오브젝트에 옮기기

        // 6. save
    }


    @Transactional
    public PostRespDto 게시글목록보기(Integer pageOwnerId, Pageable pageable) {

        Page<Post> postsEntity = postRepository.findByUserId(pageOwnerId, pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(pageOwnerId);
        List<Integer> pageNumbers = new ArrayList<>();
        for(int i=0; i<postsEntity.getTotalPages(); i++){
            pageNumbers.add(i);
        }

        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity,
                pageOwnerId,
                postsEntity.getNumber()-1,
                postsEntity.getNumber()+1,
                pageNumbers,
                0L
        );
                
        //방문자 카운터 증가 
        visitIncrease(pageOwnerId);


        return postRespDto;
    }


    public PostRespDto 게시글카테고리별보기(Integer pageOwnerId, Integer categoryId, Pageable pageable) {
        Page<Post> postsEntity = postRepository.findByUserIdAndCategoryId(pageOwnerId, categoryId, pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(pageOwnerId);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity,
                pageOwnerId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers,
                0L);

                 //방문자 카운터 증가 
                 visitIncrease(pageOwnerId);
        
        return postRespDto;
    }
    

      @Transactional
      public PostDetailRespDto 게시글상세보기(Integer id, User principal) {

          PostDetailRespDto postDetailRespDto = new PostDetailRespDto();
      
          // 게시글 가져오기
          Post postEntity = basicFindById(id);

          //권한체크
          boolean isAuth = authCheck(postEntity.getUser().getId(), principal.getId());

          
          //방문자수 증가
          visitIncrease(postEntity.getUser().getId());

          //return값
          postDetailRespDto.setPost(postEntity);
          postDetailRespDto.setPageOwner(isAuth);

          return postDetailRespDto;
    }

      @Transactional
          public PostDetailRespDto 게시글상세보기(Integer id) {
              PostDetailRespDto postDetailRespDto = new PostDetailRespDto();

        //게시글 찾기
        Post postEntity = basicFindById(id);
        //방문자수 증가
        visitIncrease(postEntity.getUser().getId());

        //return 값 만들기
        postDetailRespDto.setPost(postEntity);
        postDetailRespDto.setPageOwner(false);

        return postDetailRespDto;
    }
    
 @Transactional
 public void 게시글삭제(Integer id, User principal) {

     //게시글 확인
     Post postEntity = basicFindById(id);

     // 권한 체크
     if (authCheck(postEntity.getUser().getId(), principal.getId())) {
         postRepository.deleteById(id);
     } else {
         throw new CustomApiException("삭제 권한이 없습니다.");
     }

 }

    //게시글 한 건 찾기 
    private Post basicFindById(Integer postId) {
        Optional<Post> postOp = postRepository.findById(postId);
        if (postOp.isPresent()) {
            Post postEntity = postOp.get();
            return postEntity;
        } else {
            throw new CustomApiException("해당 게시글이 존재하지 않습니다");
        }
    }


    // 로그인 유저가 게시글 주인인지 확인하는 메서드
    private boolean authCheck(Integer principalId, Integer pageOwnerId) {
        boolean isAuth = false;
        if (principalId == pageOwnerId) {
            isAuth = true;
        } else {
            isAuth = false;
        }
        return isAuth;
    }

    // 방문자수 증가
    private void visitIncrease(Integer pageOwnerId) {
        Optional<Visit> visitOp = visitRepository.findById(pageOwnerId);
        if (visitOp.isPresent()) {
            Visit visitEntity = visitOp.get();
            Long totalCount = visitEntity.getTotalCount();
            visitEntity.setTotalCount(totalCount + 1);
        } else {
            log.error("미친 심각", "회원가입할때 Visit이 안 만들어지는 심각한 오류가 있습니다.");
            // sms 메시지 전송
            // email 전송
            // file 쓰기
            throw new CustomException("일시적 문제가 생겼습니다. 관리자에게 문의해주세요.");
        }
    }
}
