package site.metacoding.blogv3.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Service
public class PostService {

    @Value("${file.path}")
    private String uploadFolder;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    

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


    public PostRespDto 게시글목록보기(Integer userId, Pageable pageable) {

        Page<Post> postsEntity = postRepository.findByUserId(userId, pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(userId);

        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity);
        return postRespDto;
    }


        public PostRespDto 게시글카테고리별보기(Integer userId, Integer categoryId, Pageable pageable) {
        Page<Post> postsEntity = postRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(userId);

        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity);
        return postRespDto;
    }
}
