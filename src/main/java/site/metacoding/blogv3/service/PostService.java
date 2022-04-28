package site.metacoding.blogv3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.samskivert.mustache.Mustache.Visitor;

import org.apache.logging.log4j.Logger;
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
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
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
        Optional<User> pageOwnerOp = userRepository.findById(pageOwnerId);
        if (pageOwnerOp.isPresent()) {
            User pageOwnerEntity = pageOwnerOp.get();

            Optional<Visit> visitOp = visitRepository.findById(pageOwnerEntity.getId());
               

            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                //dto에 방문자수 담기(request에서 ip주소 받아서 동일하면 증가 안되는 로직!)
                postRespDto.setTotalCount(visitEntity.getTotalCount());
                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("terrible", "회원가입할 때 visit이 안만들어지는 심각한 오류");
                //sms 메세지 전송
                //email 전송
                //file 쓰기
                throw new CustomException("일시적인 오류가 발생하였습니다. 관리자에게 문의바랍니다.");
                        }
                    } else {
                        throw new CustomException("해당 블로그는 없는 페이지 입니다");
        }
         
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
        Optional<User> pageOwnerOp = userRepository.findById(pageOwnerId);
        if (pageOwnerOp.isPresent()) {
            User pageOwnerEntity = pageOwnerOp.get();

            Optional<Visit> visitOp = visitRepository.findById(pageOwnerEntity.getId());
               

            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                //dto에 방문자수 담기(request에서 ip주소 받아서 동일하면 증가 안되는 로직!)
                postRespDto.setTotalCount(visitEntity.getTotalCount());
                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("terrible", "회원가입할 때 visit이 안만들어지는 심각한 오류");
                //sms 메세지 전송
                //email 전송
                //file 쓰기
                throw new CustomException("일시적인 오류가 발생하였습니다. 관리자에게 문의바랍니다.");
                        }
                    } else {
                        throw new CustomException("해당 블로그는 없는 페이지 입니다");
                    }
        
        return postRespDto;
    }
    

    @Transactional
       public Post 게시글상세보기(Integer id) {
        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();
  
            //방문자 카운터 증가 
            Optional<Visit> visitOp = visitRepository.findById(postEntity.getUser().getId());
            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("terrible", "회원가입할 때 visit이 안만들어지는 심각한 오류");
                //sms 메세지 전송
                //email 전송
                //file 쓰기
                throw new CustomException("일시적인 오류가 발생하였습니다. 관리자에게 문의바랍니다.");
                        }
            return postEntity;
        } else {
            throw new CustomException("해당 게시글을 찾을 수 없습니다");
        }
    }
}
