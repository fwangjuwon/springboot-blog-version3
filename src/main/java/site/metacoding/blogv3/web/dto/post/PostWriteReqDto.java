package site.metacoding.blogv3.web.dto.post;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostWriteReqDto {
    
    @Size(min=1, max=60)
    @NotBlank
    private String title;

    private MultipartFile thumnailFile; //썸네일은 null허용 
    
    private String content; //null 허용

}
