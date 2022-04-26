package site.metacoding.blogv3;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

public class UtilPostTest {
 
@Test
    public void getContentWithoutImg_테스트() {
        //1. 가짜 데이터
        String html = "안녕<img src='#'> 반가워"; //목적은 이미지 태그 날리는 것!!
        //제이스프를 써보자
        Document doc = Jsoup.parse(html);

        //System.out.println(doc);
        //2. 실행
        Elements els = doc.select("img");

        for(Element el : els){
            el.remove();
        }

        System.out.println(doc);
        
        //3. 검증
        Elements elsVerify = doc.select("img");
        assertTrue(elsVerify.size()==0);

    }
}
